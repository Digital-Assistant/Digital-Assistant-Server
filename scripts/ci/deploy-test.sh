#!/usr/bin/env bash
# Deploy one image digest to the TEST tier through AWS SSM Run Command.
#   scripts/ci/deploy-test.sh <component> <sha256:digest>
# Runs only in a GitHub job with `id-token: write` in the `test` environment. No AWS key is stored:
# the job's OIDC token is exchanged for short-lived credentials of the deploy role, which may send
# only the one SSM document to the TEST box. The box validates the inputs again and runs
# /opt/udan/bin/deploy-test. Needs: curl, jq, aws CLI.
#
# Required environment (repository variables, passed by deploy-test.yml). This repo is public, so the
# values live in GitHub, never here:
#   AWS_ROLE_ARN   role the job assumes (holds the AWS account id)
#   INSTANCE_ID    the TEST instance the command is sent to
#   SSM_DOCUMENT   the SSM document that performs the deploy
# Optional env: AWS_REGION (default ap-south-1), DEPLOY_TIMEOUT_MIN (default 20).
set -euo pipefail

AWS_REGION="${AWS_REGION:-ap-south-1}"
TIMEOUT_MIN="${DEPLOY_TIMEOUT_MIN:-20}"
POLL_SECONDS=5

[ "$#" -eq 2 ] || { echo "usage: deploy-test.sh <server|nestjs> <sha256:digest>" >&2; exit 2; }
component="$1"
digest="$2"
# Check before any AWS call: the three deployment targets come from repository variables, so a
# missing or malformed one stops here with a message instead of a failed AWS call.
: "${AWS_ROLE_ARN:?set the repository variable AWS_ROLE_ARN (the role this job assumes)}"
: "${INSTANCE_ID:?set the repository variable INSTANCE_ID (the TEST instance)}"
: "${SSM_DOCUMENT:?set the repository variable SSM_DOCUMENT (the deploy document)}"
[[ "${component}" =~ ^(server|nestjs)$ ]] || { echo "component must be server or nestjs" >&2; exit 2; }
[[ "${digest}" =~ ^sha256:[0-9a-f]{64}$ ]] || { echo "digest must match sha256:<64 hex>" >&2; exit 2; }
[[ "${TIMEOUT_MIN}" =~ ^[0-9]+$ ]] || { echo "DEPLOY_TIMEOUT_MIN must be a number" >&2; exit 2; }
[[ "${AWS_ROLE_ARN}" =~ ^arn:aws:iam::[0-9]{12}:role/[A-Za-z0-9+=,.@_/-]+$ ]] \
  || { echo "AWS_ROLE_ARN must look like arn:aws:iam::<account>:role/<name>" >&2; exit 2; }
[[ "${INSTANCE_ID}" =~ ^i-[0-9a-f]{8,17}$ ]] || { echo "INSTANCE_ID must look like i-<hex>" >&2; exit 2; }
[[ "${SSM_DOCUMENT}" =~ ^[A-Za-z0-9_.-]+$ ]] || { echo "SSM_DOCUMENT must be a document name" >&2; exit 2; }
: "${ACTIONS_ID_TOKEN_REQUEST_URL:?not in a job with id-token: write}" "${ACTIONS_ID_TOKEN_REQUEST_TOKEN:?}"

token="$(curl -fsS -H "Authorization: bearer ${ACTIONS_ID_TOKEN_REQUEST_TOKEN}" \
  "${ACTIONS_ID_TOKEN_REQUEST_URL}&audience=sts.amazonaws.com" | jq -r .value)"
echo "::add-mask::${token}"

creds="$(aws sts assume-role-with-web-identity --region "${AWS_REGION}" \
  --role-arn "${AWS_ROLE_ARN}" --role-session-name "gha-deploy-${GITHUB_RUN_ID:-local}" \
  --web-identity-token "${token}" --duration-seconds 3600 \
  --query Credentials --output json)"
AWS_ACCESS_KEY_ID="$(jq -r .AccessKeyId <<<"${creds}")"
AWS_SECRET_ACCESS_KEY="$(jq -r .SecretAccessKey <<<"${creds}")"
AWS_SESSION_TOKEN="$(jq -r .SessionToken <<<"${creds}")"
echo "::add-mask::${AWS_SECRET_ACCESS_KEY}"
echo "::add-mask::${AWS_SESSION_TOKEN}"
export AWS_ACCESS_KEY_ID AWS_SECRET_ACCESS_KEY AWS_SESSION_TOKEN
unset creds token

echo "Deploying ${component} ${digest} to TEST"
command_id="$(aws ssm send-command --region "${AWS_REGION}" \
  --document-name "${SSM_DOCUMENT}" --instance-ids "${INSTANCE_ID}" \
  --parameters "component=${component},digest=${digest}" \
  --comment "GitHub run ${GITHUB_RUN_ID:-local}" \
  --query Command.CommandId --output text)"
echo "SSM command ${command_id}"

show_output() {
  aws ssm get-command-invocation --region "${AWS_REGION}" --command-id "${command_id}" \
    --instance-id "${INSTANCE_ID}" --query StandardOutputContent --output text || true
}

started=${SECONDS}
deadline=$((SECONDS + TIMEOUT_MIN * 60))
status="Pending"
while [ "${SECONDS}" -lt "${deadline}" ]; do
  sleep "${POLL_SECONDS}"
  # The invocation does not exist for a moment after send-command; treat that as Pending.
  status="$(aws ssm get-command-invocation --region "${AWS_REGION}" --command-id "${command_id}" \
    --instance-id "${INSTANCE_ID}" --query Status --output text 2>/dev/null || echo Pending)"
  echo "status: ${status} (after $((SECONDS - started))s)"
  case "${status}" in
    Success) show_output; exit 0 ;;
    Failed|Cancelled|TimedOut|Cancelling) show_output; echo "Deploy ended with status ${status}" >&2; exit 1 ;;
    *) ;;
  esac
done
show_output
echo "Gave up waiting after ${TIMEOUT_MIN} minutes (last status ${status}). The deploy may still be running on the box." >&2
exit 1
