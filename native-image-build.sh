#!/usr/bin/env bash

sbt "test; cli/assembly"
sbt "cli/assembly"

cp ./target/kprojekt-cli.jar /home/gchudnov/Downloads/bld/artifact/kprojekt-cli.jar

native-image \
  --verbose \
  --initialize-at-build-time \
  --no-fallback \
  --allow-incomplete-classpath \
  -H:+ReportUnsupportedElementsAtRuntime \
  -H:+ReportExceptionStackTraces \
  -H:ResourceConfigurationFiles=/home/gchudnov/Projects/kprojekt/res/gvm-resources.json \
  -jar ./artifact/kprojekt-cli.jar kprojekt-cli
