#!/bin/bash -l

echo "Setting up environment variables to log.."
export CRITEO_LOGGING=true
echo "Running a clean build on the mochi project for presubmit"
./gradlew clean build assembleAndroidTest --info --stacktrace
