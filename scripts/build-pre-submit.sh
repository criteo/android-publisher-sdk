#!/bin/bash -l

echo "Setting up enviorenment variables to log.."
export CRITEO_LOGGING=true
echo "Running a clean build on the mochi project for presubmit"
./gradlew clean build assembleAndroidTest --info --stacktrace
