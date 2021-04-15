#!/bin/sh

java \
    -Duser.timezone="UTC" \
    -jar target/speech-api-upload-*.jar \
    "$@"
