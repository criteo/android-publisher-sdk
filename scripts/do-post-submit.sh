#!/bin/bash -l

# Run this script to assemble and publish artifacts to Criteo nexus.
# If you run this script manually, make sure to have the Android tools available. Else, you may
# instead run the ./scripts/build-merged.sh script, to wrap the build in a docker container with
# sufficient tooling.

set -Eeuo pipefail

echo "Building and publishing artifacts to nexus"
ORIGINAL_VERSION="3.4.0"
VERSION="$ORIGINAL_VERSION-criteo-$(date -u +%Y%m%d%H%M%S)"

./scripts/do-pre-submit.sh

# FIXME EE-944 Is pushing on prod instead of preprod nexus expected ?
# Only publish to Nexus Prod acting as a PreProd environment
./gradlew publishAllPublicationsToNexusProdRepository -Pversion="$VERSION"