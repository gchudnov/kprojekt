#!/usr/bin/env bash

rm -f ./target/kprojekt-cli.jar
rm -f ./kprojekt-cli
sbt "test; cli/assembly"

# 21.0.0.2.r11-grl

native-image \
  --verbose \
  --initialize-at-build-time \
  --no-fallback \
  --allow-incomplete-classpath \
  -H:+ReportUnsupportedElementsAtRuntime \
  -H:+ReportExceptionStackTraces \
  -jar ./target/kprojekt-cli.jar kprojekt-cli
