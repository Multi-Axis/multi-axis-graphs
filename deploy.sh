#!/bin/sh

HOST="ohtu@85.23.130.197"
BIN="~/multiaxis-dist"
TMP="~/multiaxis-dist.tmp"

set -e

go build -o multiaxis-dist zab2.go

scp multiaxis-dist $HOST:$TMP

ssh $HOST "cd multi-axis-graphs; git pull; killall multiaxis-dist; mv $TMP $BIN; $BIN </dev/null >multiaxis-dist.log 2>&1 &"
