# BUILD

Building kprojekt:

## Assembly

```bash
sbt cli/assembly
```

will produce an application, that depends on JDK, bundled in a shell-script: `target/kprojekt-cli`.

```bash
./target/kprojekt-cli --help

KProjekt Cli v2.0.0 -- Visualize Kafka Topology
...
```

## GraalVM

1. Go to the root directory of the project.

2. Create the directory `META-INF/native-image` to track execution of `native-image-agent`:

```bash
export META_INF_DIR=./cli/src/main/resources/META-INF/native-image
mkdir -p "${META_INF_DIR}"
```

3. Having `java` pointing to `GraalVM`, run the app with `native-image-agent` to trace execution.

```bash
sdk use java 22.3.r19-grl
gu install native-image

# stage your app so you can run it locally without having the app packaged
sbt stage

export APP_BIN_DIR="./cli/target/universal/stage/bin"
JAVA_OPTS=-agentlib:native-image-agent=config-output-dir="${META_INF_DIR}" "${APP_BIN_DIR}/kprojekt-cli" -- --verbose ./res/example/word-count.log
```

After execution, `META-INF/native-image` directory will have a set of files for `native-image`.

4. Open `reflect-config.json` and remove lines with `Lambda` text inside.
   These entries could be different from compilation to compilation, generate warnings during native image building and are likely not required.

```bash
# jq >= 1.6

export REFLECT_CONFIG_FILE=./cli/src/main/resources/META-INF/native-image/reflect-config.json
jq 'del( .[] | select(.name | contains("Lambda")))' < "${REFLECT_CONFIG_FILE}" > "${REFLECT_CONFIG_FILE}.bak"
mv "${REFLECT_CONFIG_FILE}.bak" "${REFLECT_CONFIG_FILE}"
```

5. Execute build:

```bash
./native-image-build.sh
```

When building with `native-image`, the files:

```
-H:JNIConfigurationResources=META-INF/native-image/jni-config.json \
-H:ReflectionConfigurationResources=META-INF/native-image/reflect-config.json \
-H:ResourceConfigurationResources=META-INF/native-image/resource-config.json \
-H:DynamicProxyConfigurationResources=META-INF/native-image/proxy-config.json \
-H:SerializationConfigurationResources=META-INF/native-image/serialization-config.json \
```

will be picked up automatically.

6. Verify that the native app works:

```bash
export EXAMPLE_DIR="./res/example"
export APP_BUILD_DIR="./cli/target/graalvm-native-image"

# should print help
${APP_BUILD_DIR}/kprojekt-cli --help

# generate an image from the topology 
${APP_BUILD_DIR}/kprojekt-cli --verbose "./res/example/word-count.log"

# copy
cp .${APP_BUILD_DIR}/kprojekt-cli /usr/local/bin/
```
