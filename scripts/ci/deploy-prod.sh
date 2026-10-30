#!/usr/bin/env bash
# Deploy one component to PROD through AWS SSM Run Command.
#   scripts/ci/deploy-prod.sh <server|nestjs> [<sha256:digest>]
# Empty digest: the box deploys the digest TEST is running now. The box refuses any digest that has not
# run on TEST (or is not PROD's own current/previous, for a rollback).
# Runs only in a GitHub job with `id-token: write` in the `production` environment (deploy-prod.yml),
# after a required reviewer approved. No AWS key is stored: the job's OIDC token is exchanged for
# short-lived credentials of the PROD deploy role, which may send only the PROD SSM document to the
# box. The box validates the inputs again and runs /opt/udan/bin/deploy-prod. Needs: curl, jq, aws CLI.
#
# Required environment (repository variables, passed by deploy-prod.yml). This repo is public, so the
# values live in GitHub, never here:
#   AWS_ROLE_ARN   PROD deploy role the job assumes (holds the AWS account id)
#   INSTANCE_ID    the instance the command is sent to
#   SSM_DOCUMENT   the PROD SSM document that performs the deploy
# Optional env: AWS_REGION (default ap-south-1), DEPLOY_TIMEOUT_MIN (default 45).
# Writes a summary table (digest, previous digest, health) to GITHUB_STEP_SUMMARY when it is set.
set -euo pipefail

AWS_REGION="${AWS_REGION:-ap-south-1}"
TIMEOUT_MIN="${DEPLOY_TIMEOUT_MIN:-45}"
POLL_SECONDS=10

[ "$#" -ge 1 ] && [ "$#" -le 2 ] || { echo "usage: deploy-prod.sh <server|nestjs> [<sha256:digest>]" >&2; exit 2; }
component="$1"
digest="${2:-}"
: "${AWS_ROLE_ARN:?set the repository variable for the PROD deploy role}"
: "${INSTANCE_ID:?set the repository variable INSTANCE_ID}"
: "${SSM_DOCUMENT:?set the repository variable for the PROD deploy document}"
[[ "${component}" =~ ^(server|nestjs)$ ]] || { echo "component must be server or nestjs" >&2; exit 2; }
[[ -z "${digest}" || "${digest}" =~ ^sha256:[0-9a-f]{64}$ ]] || { echo "digest must be empty or sha256:<64 hex>" >&2; exit 2; }
[[ "${TIMEOUT_MIN}" =~ ^[0-9]+$ ]] || { echo "DEPLOY_TIMEOUT_MIN must be a number" >&2; exit 2; }
[[ "${AWS_ROLE_ARN}" =~ ^arn:aws:iam::[0-9]{12}:role/[A-Za-z0-9+=,.@_/-]+$ ]] \
  || { echo "the PROD role must look like arn:aws:iam::<account>:role/<name>" >&2; exit 2; }
[[ "${INSTANCE_ID}" =~ ^i-[0-9a-f]{8,17}$ ]] || { echo "INSTANCE_ID must look like i-<hex>" >&2; exit 2; }
[[ "${SSM_DOCUMENT}" =~ ^[A-Za-z0-9_.-]+$ ]] || { echo "the PROD document must be a document name" >&2; exit 2; }
: "${ACTIONS_ID_TOKEN_REQUEST_URL:?not in a job with id-token: write}" "${ACTIONS_ID_TOKEN_REQUEST_TOKEN:?}"

token="$(curl -fsS -H "Authorization: bearer ${ACTIONS_ID_TOKEN_REQUEST_TOKEN}" \
  "${ACTIONS_ID_TOKEN_REQUEST_URL}&audience=sts.amazonaws.com" | jq -r .value)"
echo "::add-mask::${token}"

creds="$(aws sts assume-role-with-web-identity --region "${AWS_REGION}" \
  --role-arn "${AWS_ROLE_ARN}" --role-session-name "gha-deploy-prod-${GITHUB_RUN_ID:-local}" \
  --web-identity-token "${token}" --duration-seconds 3600 \
  --query Credentials --output json)"
AWS_ACCESS_KEY_ID="$(jq -r .AccessKeyId <<<"${creds}")"
AWS_SECRET_ACCESS_KEY="$(jq -r .SecretAccessKey <<<"${creds}")"
AWS_SESSION_TOKEN="$(jq -r .SessionToken <<<"${creds}")"
echo "::add-mask::${AWS_SECRET_ACCESS_KEY}"
echo "::add-mask::${AWS_SESSION_TOKEN}"
export AWS_ACCESS_KEY_ID AWS_SECRET_ACCESS_KEY AWS_SESSION_TOKEN
unset creds token

# An empty digest is left out, so the document's default (empty: what TEST runs) applies.
params="$(jq -cn --arg c "${component}" --arg d "${digest}" '{component: [$c]} + (if $d == "" then {} else {digest: [$d]} end)')"
echo "Deploying ${component} ${digest:-<the digest TEST runs now>} to PROD"
command_id="$(aws ssm send-command --region "${AWS_REGION}" \
  --document-name "${SSM_DOCUMENT}" --instance-ids "${INSTANCE_ID}" \
  --parameters "${params}" \
  --comment "GitHub run ${GITHUB_RUN_ID:-local} by ${GITHUB_ACTOR:-unknown}" \
  --query Command.CommandId --output text)"
echo "SSM command ${command_id}"

output=""
fetch_output() {
  output="$(aws ssm get-command-invocation --region "${AWS_REGION}" --command-id "${command_id}" \
    --instance-id "${INSTANCE_ID}" --query StandardOutputContent --output text 2>/dev/null || true)"
  printf '%s\n' "${output}"
}
result_field() { sed -n -E "s/.* $1=([^ ]*).*/\1/p" <<<"$2"; }   # result_field <name> <RESULT line>
summary() {  # summary <status>: digest -> previous digest -> health, from the box's RESULT line
  [ -n "${GITHUB_STEP_SUMMARY:-}" ] || return 0
  local line d p h r o
  line="$(grep -m1 '^RESULT ' <<<"${output}" || true)"
  d="$(result_field digest "${line}")"; p="$(result_field previous "${line}")"; h="$(result_field health "${line}")"
  r="$(result_field rollback "${line}")"; o="$(result_field others "${line}")"
  {
    echo "### PROD ${component}: $1"
    echo ""
    echo "| Digest | Previous digest | Health | Rollback | Other containers |"
    echo "|---|---|---|---|---|"
    echo "| \`${d:-unknown}\` | \`${p:-unknown}\` | ${h:-unknown} | ${r:-none} | ${o:-unknown} |"
    echo ""
    echo "SSM command \`${command_id}\`."
  } >>"${GITHUB_STEP_SUMMARY}"
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
    Success) fetch_output; summary "deployed"; exit 0 ;;
    Failed|Cancelled|TimedOut|Cancelling)
      fetch_output; summary "FAILED (${status})"; echo "Deploy ended with status ${status}" >&2; exit 1 ;;
    *) ;;
  esac
done
fetch_output
summary "UNKNOWN (gave up waiting)"
echo "Gave up waiting after ${TIMEOUT_MIN} minutes (last status ${status}). The deploy may still be running on the box." >&2
exit 1
