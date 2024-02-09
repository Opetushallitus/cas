#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function main {
  local -r ENV="$1"
  shift
  local -r SERVICE="cas"

  pushd "$repo/../cloud-base"
  copy_mfa_token_to_clipboard
  awslogs get "${ENV}-app-${SERVICE}" --profile "oph-${ENV}" "$@"
  popd
}

function awslogs {
  docker build --tag awslogs - <<EOF
FROM python:3.11
RUN apt-get update && apt-get -y install
RUN pip3 install awslogs==0.14.0
ENTRYPOINT ["awslogs"]
EOF
  docker run --rm --interactive \
    --volume "${HOME}/.aws:/root/.aws" \
    --env AWS_REGION=eu-west-1 \
    awslogs "$@"
}

function copy_mfa_token_to_clipboard {
  info "Copying MFA token to clipboard..."
  op item get "AWS OPH" --otp | pbcopy
  info "Copied!"
}

function info {
  log "INFO" "$1"
}

function fatal {
  log "ERROR" "$1"
  exit 1
}

function log {
  local -r level="$1"
  local -r message="$2"
  local -r timestamp=$(date +"%Y-%m-%d %H:%M:%S")

  >&2 echo -e "${timestamp} ${level} ${message}"
}

main "$@"
