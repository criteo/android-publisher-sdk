#!/bin/bash -l

set -xEeuo pipefail

RELEASE_VERSION=$1
RELEASE_VERSION_WITH_UNDERSCORE=${RELEASE_VERSION//./_}

echo "$RELEASE_VERSION"
exit 0

git fetch origin
git checkout remotes/origin/v$RELEASE_VERSION_WITH_UNDERSCORE

./scripts/do-pre-submit.sh
./gradlew clean publishReleasePublicationToAzureRepository -PpubSdkVersion=$RELEASE_VERSION

