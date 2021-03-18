#!/usr/bin/env bash

sbt "test; cli/assembly"

# 21.0.0.2.r11-grl

native-image \
  --verbose \
  --initialize-at-build-time \
  --no-fallback \
  --allow-incomplete-classpath \
  -H:+ReportUnsupportedElementsAtRuntime \
  -H:+ReportExceptionStackTraces \
  -H:ResourceConfigurationFiles=./res/graalvm/resources.json \
  -jar ./target/kprojekt-cli.jar kprojekt-cli
