#!/bin/sh
set -eu

MAVEN_VERSION=3.9.16
MAVEN_SHA512=831a8591fe20c8243b1dbe7d71e3244f31d1665b0804b2e825e38cbbe5ce0cafb8338851f90780735568773e0a6cd07bbec107cda0b896b008b861075358b6f6
PROJECT_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
MAVEN_ROOT="$PROJECT_ROOT/.cache/apache-maven-$MAVEN_VERSION"
ARCHIVE="$PROJECT_ROOT/.cache/apache-maven-$MAVEN_VERSION-bin.tar.gz"
URL="https://dlcdn.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz"

if [ ! -x "$MAVEN_ROOT/bin/mvn" ]; then
  mkdir -p "$PROJECT_ROOT/.cache"
  if [ ! -f "$ARCHIVE" ]; then
    curl --fail --location --silent --show-error "$URL" --output "$ARCHIVE"
  fi
  printf '%s  %s\n' "$MAVEN_SHA512" "$ARCHIVE" | shasum -a 512 --check --status
  tar -xzf "$ARCHIVE" -C "$PROJECT_ROOT/.cache"
fi

exec "$MAVEN_ROOT/bin/mvn" -Dmaven.repo.local="$PROJECT_ROOT/.cache/m2" "$@"
