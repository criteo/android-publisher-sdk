#!/bin/bash -l

# Run this script to clean, build, and assemble android tests.
# You do not need to set up any environment, as a docker container will execute it.

set -Eeuo pipefail

# Go at the root of the mochi directory
cd "$(dirname "$0")/.."

./scripts/do-docker-build.sh pre-submit