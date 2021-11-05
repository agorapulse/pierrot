
#!/usr/bin/env bash

function uploadFile() {
    echo "file: $1"
    curl \
    -H "Authorization: token $GITHUB_TOKEN" \
    -H "Content-Type: $(file -b --mime-type $1)" \
    --data-binary @$1 \
    "$UPLOAD_URL?name=$(basename $1)"
}

set -e
set -x

uploadFile "$ARTIFACT_DIRECTORY/pierrot-darwin-amd64.zip"
uploadFile "$ARTIFACT_DIRECTORY/pierrot-linux-amd64.zip"
uploadFile "$ARTIFACT_DIRECTORY/pierrot-win-amd64.zip"
