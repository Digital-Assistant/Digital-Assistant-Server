#!/usr/bin/env bash
# Deploy one image digest to the TEST tier through AWS SSM Run Command.
#   scripts/ci/deploy-test.sh <component> <sha256:digest>
# Runs only in a GitHub job with `id-token: write` in the `test` environment. No AWS key is stored:
# the job's OIDC token is exchanged for short-lived credentials of the role gha-deploy-test, which
# may send only the SSM document UDAN-DeployTest to the TEST box. The box validates the inputs again
# and runs /opt/udan/bin/deploy-test. Needs: curl, jq, aws CLI.
# Optional env: DEPLOY_TIMEOUT_MIN (default 20).
set -euo pipefail

AWS_ROLE_ARN="${AWS_ROLE_ARN:-arn:aws:iam::763435626940:role/gha-deploy-test}"
AWS_REGION="${AWS_REGION:-ap-south-1}"
INSTANCE_ID="${INSTANCE_ID:-i-0430baf09645f3502}"
DOCUMENT="UDAN-DeployTest"
TIMEOUT_MIN="${DEPLOY_TIMEOUT_MIN:-20}"
POLL_SECONDS=5

[ "$#" -eq 2 ] || { echo "usage: deploy-test.sh <server|nestjs> <sha256:digest>" >&2; exit 2; }
component="$1"
digest="$2"
# Check before any AWS call.
[[ "${component}" =~ ^(server|nestjs)$ ]] || { echo "component must be server or nestjs" >&2; exit 2; }
[[ "${digest}" =~ ^sha256:[0-9a-f]{64}$ ]] || { echo "digest must match sha256:<64 hex>" >&2; exit 2; }
[[ "${TIMEOUT_MIN}" =~ ^[0-9]+$ ]] || { echo "DEPLOY_TIMEOUT_MIN must be a number" >&2; exit 2; }
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
  --document-name "${DOCUMENT}" --instance-ids "${INSTANCE_ID}" \
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
