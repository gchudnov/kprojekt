
"""This plugin is useful for building parts that use sbt.
The sbt build system is commonly used to build Scala projects.
The plugin requires a build.sbt in the root of the source tree.
"""

import tempfile
import shutil
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

    def __init__(self, name, options, project):
        super().__init__(name, options, project)

        self._assembly_dir = os.path.join(self.partdir, "cli/target/scala-2.13/")
        self._src_dir = os.path.join(self.partdir, "src")

    def pull(self):
        super().pull()
        self._setup_dependencies()

    def build(self):
        super().build()
        self._run_in_bash("""
            source "/root/.sdkman/bin/sdkman-init.sh" &&
            cd {} &&
            sbt assembly
            """.format(self._src_dir))
        install_bin_path = os.path.join(self.installdir, "bin")
        os.makedirs(install_bin_path, exist_ok=True)
        binary_path = "{}/kprojekt-cli".format(self._assembly_dir)
        shutil.copy2(binary_path, install_bin_path)

    def _setup_dependencies(self):
        self._run_in_bash("""curl -s "https://get.sdkman.io" | bash""")
        self._run_in_bash(
            """source "/root/.sdkman/bin/sdkman-init.sh" &&
            sdk install java 11.0.5.hs-adpt &&
            sdk install scala 2.13.1 &&
            sdk install sbt 1.3.3""")
        return

    def _run_in_bash(self, command, cwd=None, env=None):
        with tempfile.NamedTemporaryFile(mode="w") as f:
            f.write("set -e\n")
            f.write("{}\n".format(command))
            f.flush()
            self.run(["/bin/bash", f.name], cwd=cwd, env=env)

    def clean_build(self):
        super().clean_build()

