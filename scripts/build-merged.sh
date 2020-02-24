#!/bin/bash -l

# Run this script to assemble and publish artifacts to Criteo nexus.
# You do not need to set up any environment, as a docker container will execute it.

# Explicitly bound environment variables
MAVEN_USER="${MAVEN_USER}"
MAVEN_PASSWORD="${MAVEN_PASSWORD}"

set -Eeuo pipefail

# Go at the root of the mochi directory
cd "$(dirname "$0")/.."

SCRIPT="./scripts/do-post-submit.sh"
DOCKER_IMAGE="criteo-publishersdk-android"
SRC="$(pwd)"
DST="/mochi"

# Workaround for Windows. See: https://github.com/docker/toolbox/issues/673
export MSYS_NO_PATHCONV=1

echo "Building docker image"
docker build \
    --build-arg UID="$(id -u)" \
    --build-arg GID="$(id -g)" \
    -t "${DOCKER_IMAGE}" \
    .

echo "Running post-submit in docker container"
docker run \
    --rm \
    -v "${SRC}:${DST}" \
    -w "${DST}" \
    -e "MAVEN_USER=${MAVEN_USER}" \
    -e "MAVEN_PASSWORD=${MAVEN_PASSWORD}" \
    ${DOCKER_IMAGE} \
    bash "$SCRIPT"
