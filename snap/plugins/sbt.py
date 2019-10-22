
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

# https://github.com/snapcore/snapcraft/blob/master/snapcraft/plugins/gradle.py

# https://sdkman.io/install

# https://kubuszok.com/2018/sbt-tips-and-tricks/
