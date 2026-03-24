#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
    echo "Usage: $0 v<major>.<minor>.<patch>"
    exit 1
fi

TAG="$1"
VERSION="${TAG#v}"

if ! [[ "$VERSION" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
    echo "Error: version must be in format v<major>.<minor>.<patch>"
    exit 1
fi

MAJOR="${BASH_REMATCH[1]}"
MINOR="${BASH_REMATCH[2]}"
PATCH="${BASH_REMATCH[3]}"

VERSION_CODE=$(( MAJOR * 10000 + MINOR * 100 + PATCH ))

GRADLE="app/build.gradle.kts"

sed -i '' "s/versionCode = [0-9]*/versionCode = ${VERSION_CODE}/" "$GRADLE"
sed -i '' "s/versionName = \"[^\"]*\"/versionName = \"${VERSION}\"/" "$GRADLE"

echo "Updated $GRADLE: versionCode=$VERSION_CODE, versionName=$VERSION"

git add "$GRADLE"
git commit -m "Release $TAG"

git tag "$TAG"
git push origin master
git push origin "$TAG"

echo "Tagged and pushed $TAG"
