#!/bin/sh
# Gradle wrapper script
GRADLE_VERSION="8.8"
GRADLE_DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"

# Find JAVA_HOME
if [ -z "$JAVA_HOME" ]; then
    JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
fi

# Download gradle if needed
GRADLE_HOME="$HOME/.gradle/wrapper/dists/gradle-${GRADLE_VERSION}-bin"
GRADLE_BIN="$GRADLE_HOME/gradle-${GRADLE_VERSION}/bin/gradle"

if [ ! -f "$GRADLE_BIN" ]; then
    mkdir -p "$GRADLE_HOME"
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    curl -L "$GRADLE_DIST_URL" -o "$GRADLE_HOME/gradle.zip"
    unzip -q "$GRADLE_HOME/gradle.zip" -d "$GRADLE_HOME"
    rm "$GRADLE_HOME/gradle.zip"
fi

exec "$GRADLE_BIN" "$@"
