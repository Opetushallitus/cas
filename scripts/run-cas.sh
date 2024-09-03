#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

function main {
  cd "$repo"

  use_correct_jvm_version
  wait_for_database

  docker build -t cas-virkailija .
  docker-compose --file docker-compose.yml up --force-recreate --renew-anon-volumes
}

function wait_for_database {
  while ! docker compose port postgres 5432 &> /dev/null; do
    >&2 echo "Waiting for database to start..."
    sleep 1
  done
}

function use_correct_jvm_version {
  JAVA_HOME="$( /usr/libexec/java_home -v "21" )"
  export JAVA_HOME
}

main "$@"
