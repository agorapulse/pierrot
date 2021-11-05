
#!/usr/bin/env bash

function uploadFile() {
    echo "file: $1, name: $2"
    curl \
    -H "Authorization: token $GITHUB_TOKEN" \
    -H "Content-Type: $(file -b --mime-type $1)" \
    --data-binary @$1 \
    "$UPLOAD_URL?name=$2"
}

set -e
set -x

uploadFile "$ARTIFACT_DIRECTORY/pierrot-macos-latest.zip" "pierrot-darwin-$RELEASE_VERSION.zip"
uploadFile "$ARTIFACT_DIRECTORY/pierrot-ubuntu-latest.zip" "pierrot-linux-$RELEASE_VERSION.zip"
uploadFile "$ARTIFACT_DIRECTORY/pierrot-windows-latest.zip" "pierrot-windows-$RELEASE_VERSION.zip"
