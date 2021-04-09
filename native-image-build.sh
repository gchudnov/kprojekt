#!/usr/bin/env bash
set -ex

export APP_NAME=kprojekt-cli
export APP_JAR_PATH="./target/${APP_NAME}.jar"

rm -f "${APP_JAR_PATH}"
rm -f "./${APP_NAME}"
sbt "test; cli/assembly"

# 21.0.0.2.r11-grl

native-image \
  --verbose \
  --initialize-at-build-time \
  --no-fallback \
  --allow-incomplete-classpath \
  -H:+ReportUnsupportedElementsAtRuntime \
  -H:+ReportExceptionStackTraces \
  -jar "${APP_JAR_PATH}" "${APP_NAME}"
