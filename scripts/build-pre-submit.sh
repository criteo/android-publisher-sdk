#!/bin/bash

echo "Running a clean build on the mochi project for presubmit"
./gradlew clean build --info --stacktrace