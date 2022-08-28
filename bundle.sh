#!/usr/bin/env bash

jpackage \
  --input target \
  --main-jar speech-api-upload-1.1-SNAPSHOT.jar \
  --vendor "Medallia, Inc." \
  --name medallia-speech \
  --app-version 1.1.0 \
  --copyright "(c) 2021,2022 Medallia, Inc." \
  --description "A reference implementation for Medallia Speech" \
  --license-file LICENSE.md \
  --type pkg \
  --icon medallia-speech-logo.icns \
  -d output
