# KProjekt - Kafka Topology Visualization

<img src="res/images/projektor-192.png" width="192px" height="192px" align="right" />

Visualizes kafka topology.

![](https://github.com/gchudnov/kprojekt/workflows/Build/badge.svg)

<br clear="right" /><!-- Turn off the wrapping for the logo image. -->

## Usage

- Install [Graph Visualization Tools](https://graphviz.gitlab.io/).
- Download [kproject-cli executable](https://github.com/gchudnov/kprojekt/releases).
- Prepare a file with Kafka-topology (an [example](res/example/word-count.log)).
- Run _kproject-cli_ from the command line:

  ```bash
  ./kprojekt-cli ./word-count.log
  ```

- An output png-image with the same name as the topology file will be created.

![word-count-png](res/example/word-count.png)

## Command-line parameters

```text
  ./kprojekt-cli --help

  kprojekt-cli 1.0.0
  Usage: kprojekt-cli [options] <file>

  --help           prints this usage text
  --verbose        verbose mode
  --space <value>  space between nodes: [small,s; medium,m; large,l] (default: m)
  <file>           path to topology description
  --version
```

## Contact

[Grigorii Chudnov](mailto:g.chudnov@gmail.com)

## License

Distributed under the [The MIT License (MIT)](https://github.com/gchudnov/w3c-css/blob/master/LICENSE).
