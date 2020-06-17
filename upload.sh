#!/usr/bin/env bash

#####
# Define the Medallia Speech API token
API_TOKEN=""

#####
# Define the Medallia instance to which the data will be routed.
# This value will be specified by the Medallia team.
CALLBACK=""

if [ -z "${API_TOKEN}" ]
then
    echo "Missing API token!"
    exit 1
fi

if [ -z "${CALLBACK}" ]
then
    echo "Missing callback value!"
    exit 1
fi

#####
# Sanitize the input
export FILE="$1"
if [ ! -e "${FILE}" ]
then
    >&2 echo "Cannot upload file, unable to read file: ${FILE}"
    exit 1
fi

export FILE_MIME_TYPE=$(file --mime-type --brief ${FILE})
if [ -z "${FILE_MIME_TYPE}" ]
then
    >&2 echo "Cannot upload file, unable to determine mime type: ${FILE}"
    exit 1
fi

#####
# Upload the file to Medallia Speech
echo "-- Uploading ${FILE}"
curl \
    -s \
    -F "file=@${FILE};type=${FILE_MIME_TYPE}" \
    -F "token=${API_TOKEN}" \
    -F "model=eng1:callcenter" \
    -F "emotion=true" \
    -F "gender=true" \
    -F "output=json" \
    -F "diarize=true" \
    -F "callback=${CALLBACK}" \
    -X POST \
    'https://vcloud.vocitec.com/transcribe'

if [ "$?" -ne "0" ]
then
    >&2 echo "Error uploading file: ${FILE}"
    exit 1
fi
