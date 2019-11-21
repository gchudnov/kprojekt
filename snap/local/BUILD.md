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

```

## LINKS

- [writing-local-plugins](https://snapcraft.io/docs/writing-local-plugins)
- [gradle-example](https://github.com/snapcore/snapcraft/blob/master/snapcraft/plugins/gradle.py)
- [sbt-tips](https://kubuszok.com/2018/sbt-tips-and-tricks/)
