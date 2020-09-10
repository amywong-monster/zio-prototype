package examples

import scala.io.StdIn.readLine

import zio.UIO

object SimplestIOMonad {
  // IO func that won't encounter error
  def simplestIOFunc(): UIO[Unit] =
    for {
      name <- UIO.effectTotal(readLine)
      _    <- UIO.effectTotal(println(s"Hello, $name"))
    } yield ()
}
// To execute the IO monad on REPL, use `zio.Runtime.default.unsafeRunSync(func)
