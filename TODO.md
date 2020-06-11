# TODO

- fix graal vm build.
  - add resource-config.json to artifacts.
  + add logback.xml to graalvm resources
  + add 'images/cylinder.png' to graalvm resources
  + fix: 09:21:32.511 [zio-default-async-2-1707113102] ERROR ZIO.defaultLogger - ()
- add logo.
- update readme, add new word-count image.
- publish binaries.

search:
https://github.com/search?q=%22sbt%22+%22ubuntu-latest%22+%22+graalvm%22&type=Code

nativa build example:

https://github.com/scalameta/scalafmt/blob/master/build.sbt

native-image \
  --verbose \
  --initialize-at-build-time \
  --no-fallback \
  --allow-incomplete-classpath \
  -H:+ReportUnsupportedElementsAtRuntime \
  -H:+ReportExceptionStackTraces \
  -H:ResourceConfigurationFiles=/home/gchudnov/Projects/kprojekt/res/gvm-resources.json \
  -jar ./artifact/kprojekt-cli.jar kprojekt-cli


  -H:+TraceClassInitialization \
  -H:ReflectionConfigurationFiles=/home/gchudnov/Projects/kprojekt/res/gvm-reflection.json \
  -H:IncludeResources=logback.xml \
  -H:IncludeResources="logback.xml|META-INF/services/*.*" \
