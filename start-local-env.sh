#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function main {
  cd "$repo"
  local -r session="cas"
  tmux kill-session -t "$session" || true
  tmux start-server
  tmux new-session -d -s "$session"

  tmux select-pane -t 0
  tmux send-keys "$repo/scripts/run-database.sh" C-m

  tmux splitw -v
  tmux select-pane -t 1
  tmux send-keys "while sleep 0.5; do $repo/scripts/run-cas-java-11.sh; done" C-m

  tmux splitw -v
  tmux select-pane -t 2
  tmux send-keys "$repo/scripts/open-browser.sh; exit" C-m

  tmux attach-session -t "$session"
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
