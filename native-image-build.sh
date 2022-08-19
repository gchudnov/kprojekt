#!/usr/bin/env bash
set -ex

export DIR_SELF="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

export APP_NAME=kprojekt-cli
export APP_BUILD_DIR="${DIR_SELF}/cli/target/graalvm-native-image"
export APP_EXE_PATH="${APP_BUILD_DIR}/${APP_NAME}"

rm -f "./${APP_EXE_PATH}"
sbt "cli/graalvm-native-image:packageBin"
