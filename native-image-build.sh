#!/usr/bin/env bash
set -ex

export APP_NAME=kprojekt-cli
export APP_JAR_PATH="./target/${APP_NAME}.jar"

rm -f "${APP_JAR_PATH}"
rm -f "./${APP_NAME}"
sbt "test; cli/assembly"

# 22.2.r17-grl

RUNTIME_INIT_LIST="$(cat ./res/graalvm/init-run-time.txt | tr '\n' ',')"

native-image \
  --verbose \
  --initialize-at-run-time="${RUNTIME_INIT_LIST}" \
  --no-fallback \
  -H:+ReportUnsupportedElementsAtRuntime \
  -H:+ReportExceptionStackTraces \
  -jar "${APP_JAR_PATH}" "${APP_NAME}"
