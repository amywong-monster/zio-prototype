package examples

import zio.UIO

import scala.io.StdIn.readLine

object IOMonadicOperation {
  def func(): UIO[Unit] =
    for {
      name <- UIO.effectTotal(readLine)
      _ <- UIO.effectTotal(println(s"Hello, $name"))
    } yield ()
}
// To execute the IO monad on REPL, use `zio.Runtime.default.unsafeRunSync(func)
