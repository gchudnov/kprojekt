package com.github.gchudnov.kprojekt

import com.github.gchudnov.kprojekt.formatter.dot.{ DotConfig, DotSpace }
import com.github.gchudnov.kprojekt.zopt.SuccessExitException
import com.github.gchudnov.kprojekt.zopt.ozeffectsetup.OZEffectSetup
import com.github.gchudnov.kprojekt.zopt.ozeffectsetup.OZEffectSetup.{ displayToOut, runOEffects }
import com.github.gchudnov.kprojekt.{ BuildInfo => KBuildInfo }
import scopt.OEffect.ReportError
import scopt.{ OEffect, OParser, OParserSetup }
import zio._
import zio.config.{ ReadError, _ }
import zio.config.magnolia._
import zio.config.typesafe._

import java.io.File

final case class CliArgs(
  file: Option[File],
  space: String,
  isVerbose: Boolean
)

object CliArgs {

  private[kprojekt] val DefaultVerbose: Boolean = false
  private[kprojekt] val DefaultSpace: String    = DotSpace.asString(DotSpace.Medium)

  def empty: CliArgs =
    CliArgs(
      file = None,
      space = DefaultSpace,
      isVerbose = DefaultVerbose
    )
}

final case class CliConfig(
  file: File,
  dot: DotConfig,
  isVerbose: Boolean
)

object CliConfig {
  private val ArgHelpShort    = 'h'
  private val ArgHelpLong     = "help"
  private val ArgSpaceShort   = 's'
  private val ArgSpaceLong    = "space"
  private val ArgVerboseShort = 'v'
  private val ArgVerboseLong  = "verbose"
  private val ArgVersionLong  = "version"

  private val argsBuilder = OParser.builder[CliArgs]
  private val argsParser = {
    import argsBuilder._
    OParser.sequence(
      programName(KBuildInfo.name),
      head(KBuildInfo.name, KBuildInfo.version),
      opt[String](ArgSpaceShort, ArgSpaceLong)
        .action((x, c) => c.copy(space = x))
        .validate(x => DotSpace.parse(x).left.map(_.getMessage).map(_ => ()))
        .text(s"Nodes proximity: [${listSpacesText()}] (default: ${CliArgs.DefaultSpace.head})"),
      arg[File]("<file>")
        .required()
        .action((x, c) => c.copy(file = Some(x)))
        .text("path to topology file"),
      opt[Unit](ArgVerboseShort, ArgVerboseLong)
        .optional()
        .action((_, c) => c.copy(isVerbose = true))
        .text("verbose output"),
      opt[Unit](ArgHelpShort, ArgHelpLong)
        .optional()
        .text("prints this usage text")
        .validate(_ => Left(OEffectHelpKey)),
      opt[Unit](ArgVersionLong)
        .optional()
        .text("prints the version")
        .validate(_ => Left(OEffectVersionKey)),
      note(s"""
              |Examples:
              |
              |  - Make a PNG-image of the topology
              |    ${KBuildInfo.name} <topology-filepath>
              |""".stripMargin)
    )
  }

  private val OEffectPrefix     = "OEFFECT"
  private val OEffectHelpKey    = s"$OEffectPrefix:HELP"
  private val OEffectVersionKey = s"$OEffectPrefix:VERSION"

  def fromArgs(args: List[String])(argParserSetup: OParserSetup): RIO[OZEffectSetup, CliConfig] =
    OParser.runParser(argsParser, args, CliArgs.empty, argParserSetup) match {
      case (result, effects) =>
        for {
          pEffects <- preprocessOEffects(effects)
          _        <- runOEffects(pEffects)
          argsConf <- ZIO.fromOption(result).orElseFail(new IllegalArgumentException(s"Use --$ArgHelpLong for more information."))
          config <- for {
                      file     <- ZIO.fromOption(argsConf.file) orElseFail (new IllegalArgumentException(s"Topology file is not specified."))
                      baseConf <- loadResourceConfig()
                      isVerbose = argsConf.isVerbose
                    } yield CliConfig(
                      file = file,
                      dot = baseConf.dot,
                      isVerbose = isVerbose
                    )
        } yield config
    }

  private def listSpacesText(): String = {
    val names     = List(DotSpace.Small, DotSpace.Medium, DotSpace.Large).map(it => DotSpace.asString(it))
    val namePairs = names.map(s => s"${s},${s.head}")
    namePairs.mkString("; ")
  }

  private def preprocessOEffects(effects: List[OEffect]): RIO[OZEffectSetup, List[OEffect]] = {
    val hasHelp    = hasKey(OEffectHelpKey)(effects)
    val hasVersion = hasKey(OEffectVersionKey)(effects)

    if (hasHelp || hasVersion) {
      val value = (hasHelp, hasVersion) match {
        case (true, _) =>
          usage()
        case (false, true) =>
          version()
        case (_, _) =>
          ""
      }

      displayToOut(value) *> ZIO.fail(new SuccessExitException())
    } else
      ZIO.attempt(effects.filterNot(it => it.isInstanceOf[ReportError] && it.asInstanceOf[ReportError].msg.startsWith(OEffectPrefix)))
  }

  private def hasKey(key: String)(effects: List[OEffect]): Boolean =
    effects.exists {
      case ReportError(msg) if (msg == key) => true
      case _                                => false
    }

  private def usage(): String =
    OParser.usage(argsParser)

  private def version(): String =
    s"${KBuildInfo.name} ${KBuildInfo.version}"

  private def loadResourceConfig(): IO[ReadError[String], CliConfig] = {
    implicit def deriveForZonedDateTime: Descriptor[DotSpace] =
      Descriptor[String].transformOrFail(s => DotSpace.parse(s).left.map(_.getMessage), r => Right(DotSpace.asString(r)))

    read(descriptor[CliConfig] from TypesafeConfigSource.fromResourcePath)
  }
}
