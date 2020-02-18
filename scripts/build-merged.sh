#!/bin/bash -l

echo "Building and publishing artifacts to nexus"
ORIGINAL_VERSION="3.4.0"
VERSION="$ORIGINAL_VERSION-criteo-$(date -u +%Y%m%d%H%M%S)"
NEXUS_URL="http://nexus.criteo.prod/content/repositories/criteo.android.releases/"

./scripts/build-pre-submit.sh
./gradlew publish -Pversion="$VERSION" -PpublishUrl="$NEXUS_URL"
