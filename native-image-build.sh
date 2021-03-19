#!/usr/bin/env bash

rm -f ./target/kprojekt-cli.jar
rm -f ./kprojekt-cli
sbt "test; cli/assembly"

# 21.0.0.2.r11-grl

BUILD_INIT_LIST="$(cat ./res/graalvm/init-build-time.txt | tr '\n' ',')"
RUNTIME_INIT_LIST="$(cat ./res/graalvm/init-run-time.txt | tr '\n' ',')"

native-image \
  --verbose \
  --initialize-at-build-time \
  --no-fallback \
  --allow-incomplete-classpath \
  -H:+ReportUnsupportedElementsAtRuntime \
  -H:+ReportExceptionStackTraces \
  -jar ./target/kprojekt-cli.jar kprojekt-cli


#  --initialize-at-build-time="${BUILD_INIT_LIST}" \
#  --initialize-at-run-time="${RUNTIME_INIT_LIST}" \
