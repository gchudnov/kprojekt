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

4. Execute build