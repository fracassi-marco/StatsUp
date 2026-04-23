#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 2 ]]; then
    echo "Usage: $0 v<major>.<minor>.<patch> \"<changelog message>\""
    exit 1
fi

TAG="$1"
MESSAGE="$2"
VERSION="${TAG#v}"

if [[ -z "$MESSAGE" ]]; then
    echo "Error: changelog message cannot be empty"
    exit 1
fi

if ! [[ "$VERSION" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
    echo "Error: version must be in format v<major>.<minor>.<patch>"
    exit 1
fi

MAJOR="${BASH_REMATCH[1]}"
MINOR="${BASH_REMATCH[2]}"
PATCH="${BASH_REMATCH[3]}"

VERSION_CODE=$(( MAJOR * 10000 + MINOR * 100 + PATCH ))

GRADLE="app/build.gradle.kts"
CHANGELOG="docs/changelog.json"

perl -i -pe "s/versionCode = \d+/versionCode = ${VERSION_CODE}/" "$GRADLE"
perl -i -pe "s/versionName = \"[^\"]+\"/versionName = \"${VERSION}\"/" "$GRADLE"

echo "Updated $GRADLE: versionCode=$VERSION_CODE, versionName=$VERSION"

DATE=$(date +"%Y-%m-%d")
export CHANGELOG_VERSION="$VERSION"
export CHANGELOG_DATE="$DATE"
export CHANGELOG_MESSAGE="$MESSAGE"

python3 << 'PYEOF'
import json, os

path = os.environ.get("CHANGELOG_PATH", "docs/changelog.json")
try:
    with open(path) as f:
        entries = json.load(f)
except (FileNotFoundError, json.JSONDecodeError):
    entries = []

entries.insert(0, {
    "version": os.environ["CHANGELOG_VERSION"],
    "date": os.environ["CHANGELOG_DATE"],
    "message": os.environ["CHANGELOG_MESSAGE"]
})

with open(path, "w") as f:
    json.dump(entries, f, indent=2, ensure_ascii=False)
    f.write("\n")

print("Updated " + path)
PYEOF

git add "$GRADLE" "$CHANGELOG"
git commit -m "Release $TAG"

git tag "$TAG"
git push origin master
git push origin "$TAG"

echo "Tagged and pushed $TAG"
