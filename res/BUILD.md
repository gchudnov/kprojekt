# Native Image Building

1. Go to the root directory of the project.

2. Create the directory `META-INF/native-image` to track execution of `native-image-agent` .

```bash
mkdir -p ./cli/src/main/resources/META-INF/native-image
```

3. Having `java` pointing to `GraalVM`, run the app to trace execution.

```bash
sdk use java 21.0.0.2.r11-grl
java -agentlib:native-image-agent=config-output-dir=./cli/src/main/resources/META-INF/native-image -jar ./target/kprojekt-cli.jar
```

After execution, `META-INF/native-image` directory will have a set of files for `native-image`.

4. Open `reflect-config.json` and remove lines with `Lambda` text inside.
   These entries could be different from compilation to compilation, generate warnings during native image building and are likely not required.

5. Open `resource-config.json` and replace patterns with:

```json
    {
      "pattern": "reference.conf"
    },
    {
      "pattern": "application.conf"
    },
    {
      "pattern": "logback.xml"
    },
    {
      "pattern": "images/cylinder.png"
    }
```

6. Execute build:

```bash
./native-image-build.sh
```

After building via `native-image`, the files:

```
-H:JNIConfigurationResources=META-INF/native-image/jni-config.json \
-H:ReflectionConfigurationResources=META-INF/native-image/reflect-config.json \
-H:ResourceConfigurationResources=META-INF/native-image/resource-config.json \
-H:DynamicProxyConfigurationResources=META-INF/native-image/proxy-config.json \
-H:SerializationConfigurationResources=META-INF/native-image/serialization-config.json \
```

will be picked up automatically.

7. After building, verify that the app works:

```bash
# verify that the app works
./kprojekt-cli

# try to generate image from the topology
./kprojekt-cli ./res/example/word-count.log
```

After execution the image for the given topology should be generated (NOTE: the image is generated in the same directory where topology is located).
