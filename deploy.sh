#!/bin/sh

HOST="ohtu@85.23.130.197"
BIN="~/multiaxis-dist"
TMP="~/multiaxis-dist.tmp"

set -e

go build -o multiaxis-dist

scp multiaxis-dist $HOST:$TMP

ssh $HOST "killall multiaxis-dist; mv $TMP $BIN; $BIN </dev/null >multiaxis-dist.log 2>&1 &"
