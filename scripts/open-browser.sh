#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function main {
  wait_for_port 8080
  open "http://localhost:8080/cas/login?service=http%3A%2F%2Flocalhost%3A8101%2Fkayttooikeus-service%2Fj_spring_cas_security_check"
}

function wait_for_port {
  while ! is_port_listening "$1"; do
    sleep 2
  done
}

function is_port_listening {
  nc -z localhost "$1"
}

main "$@"
