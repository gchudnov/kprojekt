package com.github.gchudnov.kprojekt.zopt.ozeffectsetup

import zio.*
import zio.Console.*
import com.github.gchudnov.sqsmove.zopt.{ FailureExitException, SuccessExitException }

class StdioEffectSetup(console: Console) extends OZEffectSetup:

  override def displayToOut(msg: String): Task[Unit] =
    console.printLine(msg)

  override def displayToErr(msg: String): Task[Unit] =
    console.printLineError(msg)

  override def reportError(msg: String): Task[Unit] =
    displayToErr("Error: " + msg)

  override def reportWarning(msg: String): Task[Unit] =
    displayToErr("Warning: " + msg)

  override def terminate(exitState: Either[String, Unit]): Task[Unit] =
    exitState match
      case Left(_)  => ZIO.fail(new FailureExitException())
      case Right(_) => ZIO.fail(new SuccessExitException())

object StdioEffectSetup:
  def layer: ZLayer[Has[Console], Nothing, Has[OZEffectSetup]] =
    (for
      console <- ZIO.service[Console]
      service  = new StdioEffectSetup(console)
    yield service).toLayer
