
"""This plugin is useful for building parts that use sbt.
The sbt build system is commonly used to build Scala projects.
The plugin requires a build.sbt in the root of the source tree.
"""

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

        # # Add a new property called "my-property"
        # schema['properties']['my-property'] = {
        #     'type': 'string',
        # }

        # # The "my-option" property is now required
        # schema['required'].append('my-property')

        return schema

    def pull(self):
        super().pull()
        print('Look ma, I pulled! Here is "my-property": {}'.format(
            self.options.my_property))

    def build(self):
        super().build()
        print('Look ma, I built!')

    def _run_in_bash(self, commandlist, cwd=None, env=None):
        with tempfile.NamedTemporaryFile(mode="w") as f:
            f.write("set -e\n")
            f.write("exec {}\n".format(" ".join(commandlist)))
            f.flush()

            self.run(["/bin/bash", f.name], cwd=cwd, env=env)



# https://snapcraft.io/docs/writing-local-plugins

# https://github.com/snapcore/snapcraft/blob/master/snapcraft/plugins/gradle.py

# https://sdkman.io/install

# https://kubuszok.com/2018/sbt-tips-and-tricks/
