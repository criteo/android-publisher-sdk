#!/bin/bash

echo "Building and publishing artifacts to nexus"
ORIGINAL_VERSION=2.0.0
VERSION=$ORIGINAL_VERSION-criteo-$(date -u +%Y%m%d%H%M%S)
./gradlew clean build publish -Pversion=$VERSION -PpublishUrl="http://nexus.criteo.prod/content/repositories/criteo.android.releases/"