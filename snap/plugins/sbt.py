
"""This plugin is useful for building parts that use sbt.
The sbt build system is commonly used to build Scala projects.
The plugin requires a build.sbt in the root of the source tree.
"""

import tempfile
import logging
import os
import urllib.parse
from glob import glob
from typing import Sequence

import snapcraft
from snapcraft import file_utils, formatting_utils
from snapcraft.internal import errors, sources

logger = logging.getLogger(__name__)


class SbtPlugin(snapcraft.BasePlugin):

    @classmethod
    def schema(cls):
        schema = super().schema()
        return schema

    def pull(self):
        super().pull()
        self._setup_dependencies()

    def build(self):
        super().build()

    def _setup_dependencies(self):
        self._run_in_bash("""curl -s "https://get.sdkman.io" | bash""")
        self._run_in_bash(
            """source "/root/.sdkman/bin/sdkman-init.sh" && sdk install java 11.0.4.hs-adpt && sdk install scala 2.13.1 && sdk install sbt 1.3.3""")
        return

    def _run_in_bash(self, command, cwd=None, env=None):
        with tempfile.NamedTemporaryFile(mode="w") as f:
            f.write("set -e\n")
            f.write("{}\n".format(command))
            f.flush()

            self.run(["/bin/bash", f.name], cwd=cwd, env=env)


# https://snapcraft.io/docs/writing-local-plugins

# https://github.com/snapcore/snapcraft/blob/master/snapcraft/plugins/gradle.py

# https://sdkman.io/install

# https://kubuszok.com/2018/sbt-tips-and-tricks/
