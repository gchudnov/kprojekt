package com.github.gchudnov.kprojekt.zopt.ozeffectsetup

import com.github.gchudnov.kprojekt.zopt.{ FailureExitException, SuccessExitException }
import zio._

class StdioEffectSetup(console: Console) extends OZEffectSetup {

  override def displayToOut(msg: String): Task[Unit] =
    console.printLine(msg)

  override def displayToErr(msg: String): Task[Unit] =
    console.printLineError(msg)

  override def reportError(msg: String): Task[Unit] =
    displayToErr("Error: " + msg)

  override def reportWarning(msg: String): Task[Unit] =
    displayToErr("Warning: " + msg)

  override def terminate(exitState: Either[String, Unit]): Task[Unit] =
    exitState match {
      case Left(_)  => ZIO.fail(new FailureExitException())
      case Right(_) => ZIO.fail(new SuccessExitException())
    }
}

object StdioEffectSetup {
  def layer: ZLayer[Console, Nothing, OZEffectSetup] =
    (for {
      console <- ZIO.service[Console]
      service  = new StdioEffectSetup(console)
    } yield service).toLayer
}
