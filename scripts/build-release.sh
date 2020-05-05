#!/bin/bash -l

# DO NOT RUN IT MANUALLY!
# This script is expected to be run by the `pub-sdk-mochi-prod-deployment` Jenkins job.

# This script assembles and publishes artifacts to production on Azure.
# You do not need to set up any environment, as a docker container will execute it.

set -Eeuo pipefail

# Go at the root of the mochi directory
cd "$(dirname "$0")/.."

RELEASE_COMMIT_SHA1=$1

./scripts/do-docker-build.sh release "${RELEASE_COMMIT_SHA1}"