# KProjekt - Kafka Topology Visualization

<img src="res/projektor-192.png" width="192px" height="192px" align="right" />

Visualizes kafka topology.

![](https://github.com/gchudnov/kprojekt/workflows/Build/badge.svg)

<br clear="right" /><!-- Turn off the wrapping for the logo image. -->

## Usage

- Install [Graph Visualization Tools](https://graphviz.gitlab.io/).
  ```bash
  # Ubuntu
  sudo apt install graphviz
  ```
- Download and extract [kproject-cli executable](https://github.com/gchudnov/kprojekt/releases).
- Prepare a file with Kafka-topology (e.g. [./word-count.log](res/example/word-count.log)).
- Run application from the command line:

  ```bash
  ./kprojekt-cli ./word-count.log
  ```

- As an output, a dot-file and png-image will be created in the directory log was provided: `word-count.dot` and `word-count.png`.

![word-count-png](res/example/word-count.png)

## Command-line parameters

```text
./kprojekt-cli --help

KProjekt Cli v2.0.0 -- Visualize Kafka Topology

USAGE

  $ kprojekt-cli [(-v, --verbose)] <input-topology>

ARGUMENTS

  <input-topology>
    An existing file.

OPTIONS

  (-v, --verbose)
    A true or false value.

    This setting is optional. Default: 'false'.
```

## Contact

[Grigorii Chudnov](mailto:g.chudnov@gmail.com)

## License

Distributed under the [The MIT License (MIT)](LICENSE).
