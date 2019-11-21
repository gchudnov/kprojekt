# BUILD

- Install snapcraft `sudo snap install snapcraft --classic`
- Run `sudo snapcraft`

## COMMANDS

```bash
multipass list
multipass stop snapcraft-kprojekt-cli
multipass delete snapcraft-kprojekt-cli
multipass purge

sudo snapcraft build --use-lxd --debug kproject-cli
sudo snapcraft clean --use-lxd kprojekt-cli

sudo lxc list
sudo lxc delete snapcraft-kprojekt-cli
sudo lxc exec snapcraft-kprojekt-cli -- /bin/bash

snapcraft --destructive-mode
```

## STEPS

```bash
sudo snapcraft build --use-lxd --debug kproject-cli

# after building, connect to the container
sudo lxc exec snapcraft-kprojekt-cli -- /bin/bash

# run snapcraft once more
snapcraft

# Searching for local plugin for sbt
# Skipping pull kprojekt-cli (already ran)
# Skipping build kprojekt-cli (already ran)
# Staging kprojekt-cli 
# Priming kprojekt-cli 
# Snapping 'kprojekt-cli' - 
# Snapped kprojekt-cli_1.0.0_amd64.snap

# after snapping, go to your machine - a new file is created in the root directory `kprojekt-cli_1.0.0_amd64.snap`

# install to test locally
snap install --dangerous kprojekt-cli_1.0.0_amd64.snap

# go inside snap
sudo snap run --shell kprojekt-cli


```

## LINKS

- [writing-local-plugins](https://snapcraft.io/docs/writing-local-plugins)
- [gradle-example](https://github.com/snapcore/snapcraft/blob/master/snapcraft/plugins/gradle.py)
- [sbt-tips](https://kubuszok.com/2018/sbt-tips-and-tricks/)
- [build-on-lxd](https://snapcraft.io/docs/build-on-lxd)
