# BUILD

Building kprojekt:

## GraalVM

## Assembly

```bash
sbt cli/assembly
```

will produce an application, bundled in a shell-script: `target/kprojekt-cli`.

```bash
./target/kprojekt-cli --help

# kprojekt-cli 1.3.1
# Usage: kprojekt-cli [options] <file>

#   -s, --space <value>  Nodes proximity: [small,s; medium,m; large,l] (default: m)
#   <file>               path to topology file
#   -v, --verbose        verbose output
#   -h, --help           prints this usage text
#   --version            prints the version

# Examples:

#   - Make a PNG-image of the topology
#     kprojekt-cli <topology-filepath>

```
