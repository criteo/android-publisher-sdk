#!/bin/bash -l

# Run this script to assemble and publish development artifacts.
# If you run this script manually, make sure to have the Android tools available. Else, you may
# instead run the ./scripts/build-merged.sh script, to wrap the build in a docker container with
# sufficient tooling.

set -Eeuo pipefail

echo "Building and publishing development artifacts"

./scripts/do-pre-submit.sh

./gradlew bintrayUpload sendReleaseDeployedToBintrayMessageToSlack
