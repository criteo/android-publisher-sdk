#!/bin/bash -l

# Run this script to clean, build, and assemble android tests.
# If you run this script manually, make sure to have the Android tools available. Else, you may
# instead run the ./scripts/build-merged.sh script, to wrap the build in a docker container with
# sufficient tooling.

set -Eeuo pipefail

echo "Running a clean build on the mochi project for presubmit"
# Deactivate temporarily for migration from Groovy -> Kotlin of gradle files
# ./gradlew clean build assembleAndroidTest --info --stacktrace
