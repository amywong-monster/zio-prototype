package examples

import scala.io.Source
import scala.io.StdIn.readLine

import scala.util.Try

import zio.{ Task, UIO, ZIO }

object IOMayGetError {
  // IO func that will encounter error
  def loadFile: Task[String] =
    for {
      filename     <- UIO.effectTotal(readLine)
      // try entering `sample.txt` from the console to see how it goes
      // alternatively, try entering an non-exist file
      _            <- UIO.effectTotal(println(s"Loading file $filename"))
      // loading file may encounter runtime error
      // it should not happen in type safe fp
      // therefore enclose by `Try` and convert to `ZIO`
      bufferSource <- ZIO.fromTry(Try(Source.fromFile(filename)))
    } yield bufferSource.toList.mkString
}
