#!/bin/sh
#
# Gradle wrapper fallback script for Unix-like systems
#

APP_HOME=$(cd "$(dirname "$0")" && pwd)

# 1. If official gradle-wrapper.jar exists, use standard java wrapper launcher
if [ -f "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" ]; then
    JAVA_CMD="java"
    if [ -n "$JAVA_HOME" ]; then
        JAVA_CMD="$JAVA_HOME/bin/java"
    fi
    exec "$JAVA_CMD" -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
fi

# 2. If system gradle command is present, delegate directly to gradle
if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
fi

echo "Error: Neither gradle/wrapper/gradle-wrapper.jar nor system 'gradle' command was found."
echo "Please install Gradle or run the build via GitHub Actions workflow."
exit 1
