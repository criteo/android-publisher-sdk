#!/bin/bash -l

set -xEeuo pipefail

RELEASE_COMMIT_SHA1=$1

VERSION=$(./gradlew -q printPublicationVersion)
echo "Releasing $RELEASE_COMMIT_SHA1 as $VERSION"

git fetch origin "$RELEASE_COMMIT_SHA1"
git checkout FETCH_HEAD

./scripts/do-pre-submit.sh
./gradlew clean \
    publishReleasePublicationToAzureRepository \
    sendReleaseDeployedToAzureMessageToSlack

git tag -a "$VERSION" -m "Release $VERSION"
git push origin "refs/tags/$VERSION"
