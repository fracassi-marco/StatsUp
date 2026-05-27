#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DEFAULT="$ROOT/app/src/main/res/values/strings.xml"
LOCALES=("values-it" "values-es" "values-fr")
FAILED=0

get_keys() {
  grep '<string name=' "$1" | sed 's/.*name="\([^"]*\)".*/\1/' | sort
}

DEFAULT_KEYS=$(get_keys "$DEFAULT")

for locale in "${LOCALES[@]}"; do
  FILE="$ROOT/app/src/main/res/$locale/strings.xml"

  if [ ! -f "$FILE" ]; then
    echo "ERROR: missing file $FILE"
    FAILED=1
    continue
  fi

  LOCALE_KEYS=$(get_keys "$FILE")

  MISSING=$(comm -23 <(echo "$DEFAULT_KEYS") <(echo "$LOCALE_KEYS"))
  EXTRA=$(comm -13 <(echo "$DEFAULT_KEYS") <(echo "$LOCALE_KEYS"))

  if [ -n "$MISSING" ]; then
    echo "ERROR [$locale] missing keys:"
    echo "$MISSING" | sed 's/^/  - /'
    FAILED=1
  fi

  if [ -n "$EXTRA" ]; then
    echo "ERROR [$locale] extra keys not in default:"
    echo "$EXTRA" | sed 's/^/  + /'
    FAILED=1
  fi
done

if [ "$FAILED" -eq 0 ]; then
  echo "✓ All translations complete ($(echo "$DEFAULT_KEYS" | wc -l | tr -d ' ') strings across en/it/es/fr)"
fi

exit "$FAILED"
