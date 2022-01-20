
#!/usr/bin/env bash

function uploadFile() {
    echo "file: $1, name: $2"
    curl \
    -H "Authorization: token $GITHUB_TOKEN" \
    -H "Content-Type: application/zip" \
    --data-binary @$1 \
    "$UPLOAD_URL?name=$2"
}

set -e
set -x

ls -la "$ARTIFACT_DIRECTORY"

uploadFile "$ARTIFACT_DIRECTORY/pierrot-macos-latset/pierrot-darwin-amd64-v$RELEASE_VERSION.zip" "pierrot-darwin-amd64-v$RELEASE_VERSION.zip"
uploadFile "$ARTIFACT_DIRECTORY/pierrot-ubuntu-latset/pierrot-linux-amd64-v$RELEASE_VERSION.zip" "pierrot-linux-amd64-v$RELEASE_VERSION.zip"
uploadFile "$ARTIFACT_DIRECTORY/pierrot-windows-latset/pierrot-win-amd64-v$RELEASE_VERSION.zip" "pierrot-windows-amd64-v$RELEASE_VERSION.zip"
