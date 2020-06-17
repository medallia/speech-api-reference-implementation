#!/usr/bin/env bash

#####
# Uncomment this line to perform a pretend upload.
PRETEND=1

#####
# Get the list of files.
#
# If the files are stored locally, you can use this:
FILES="*/*.wav"

# If the files come from AWS S3, you can use this:
#AWS_S3_BUCKET="your-bucket-name-here"
#AWS_CREDENTIAL_PROFILE="default"
#FILES=$(aws --profile "${AWS_CREDENTIAL_PROFILE}" s3api list-objects --bucket "${AWS_S3_BUCKET}" | jq -r '.Contents[].Key')

if [ -z "${FILES}" ]
then
    echo "No files found"
    exit 0
fi

#####
# Process each file
for FILE in ${FILES}
do
    if [ "${PRETEND}" -eq "1" ]
    then
        echo "Pretend uploading ${FILE}"
        continue
    fi

    # Download the file from AWS
    if [ -n "${AWS_S3_BUCKET}" ]
    then
        # Make a temporary local directory to hold the file
        FILEDIR="temp/$(dirname "${FILE}")"
        mkdir -p "${FILEDIR}"

        aws --profile "${AWS_CREDENTIAL_PROFILE}" s3api get-object --bucket "${AWS_S3_BUCKET}" --key "${FILE}" "temp/${FILE}"
    fi

    # Upload the file to Medallia Speech
    ./upload.sh "${FILE}"

    # Remove the temporary local directory
    if [ -n "${AWS_S3_BUCKET}" ]
        if [[ "${FILEDIR}" != "." || "${FILEDIR}" != "/" ]]
        then
            rm -rf "${FILEDIR}"
        fi
    fi
done
