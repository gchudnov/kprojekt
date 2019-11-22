
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

        self._src_dir = os.path.join(self.partdir, "src")
        self._assembly_dir = os.path.join(
            self._src_dir, "cli/target/scala-2.13")
        self._install_bin_dir = os.path.join(self.installdir, "bin")

    def pull(self):
        super().pull()
        self._setup_dependencies()

    def build(self):
        super().build()
        self._run_in_bash("""
            source "/root/.sdkman/bin/sdkman-init.sh" &&
            cd {} &&
            sbt assembly
            """ .format(self._src_dir))

        os.makedirs(self._install_bin_dir, exist_ok=True)
        
        binary_path = "{}/kprojekt-cli".format(self._assembly_dir)
        shutil.copy2(binary_path, self._install_bin_dir)

    def _setup_dependencies(self):
        self._run_in_bash("""curl -s "https://get.sdkman.io" | bash""")
        self._run_in_bash(
            """source "/root/.sdkman/bin/sdkman-init.sh"

            export IS_JAVA_INSTALLED=$(sdk current | grep -c java)
            if [[ "${IS_JAVA_INSTALLED}" != "0" ]]; then
                echo "Java is already installed"
            else
                sdk install java 11.0.5.hs-adpt
            fi

            export IS_SCALA_INSTALLED=$(sdk current | grep -c scala)
            if [[ "${IS_SCALA_INSTALLED}" != "0" ]]; then
                echo "Scala is already installed"
            else
                sdk install scala 2.13.1
            fi

            export IS_SBT_INSTALLED=$(sdk current | grep -c sbt)
            if [[ "${IS_SBT_INSTALLED}" != "0" ]]; then
                echo "Sbt is already installed"
            else
                sdk install sbt 1.3.3
            fi
            """)
        return

    def _run_in_bash(self, command, cwd=None, env=None):
        with tempfile.NamedTemporaryFile(mode="w") as f:
            f.write("set -e\n")
            f.write("{}\n".format(command))
            f.flush()
            self.run(["/bin/bash", f.name], cwd=cwd, env=env)

    def clean_build(self):
        super().clean_build()
