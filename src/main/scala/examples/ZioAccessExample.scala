package examples

import scala.io.StdIn.readLine

import zio.{ UIO, ZIO }

import org.jinilover.microservice.ConfigTypes.DbConfig

object ZioAccessExample {
  // Note how it uses `ZIO.access` to access the dependency
  def contrivedFunc1(): UIO[Unit] =
    for {
      name  <- UIO.effectTotal(readLine)
      dbUrl <- ZIO
                 .access[DbConfig](dbConfig => dbConfig.url)
                 .provide(DbConfig("jdbc:postgresql://localhost:5432/postgres", "postgres", "password"))
      _     <- UIO.effectTotal(println(s"Hello, $name, do u want to connect to $dbUrl"))
    } yield ()

  // Note difference between `access` and `accessM`
  def contrivedFunc2(): UIO[Unit] =
    for {
      name <- UIO.effectTotal(readLine)
      _    <- ZIO
                .accessM[DbConfig](
                  dbConfig => UIO.effectTotal(println(s"Hello, $name, do u want to connect to ${dbConfig.url}"))
                )
                .provide(DbConfig("jdbc:postgresql://localhost:5432/postgres", "postgres", "password"))
    } yield ()

  // leave the dependency out of this function, preferred approach
  // this function can be called by contrivedFunc3().provide(anyConfigYouWant)
  def contrivedFunc3(): ZIO[DbConfig, Throwable, Unit] =
    for {
      name <- UIO.effectTotal(readLine)
      _    <- ZIO
                .accessM[DbConfig](
                  dbConfig => UIO.effectTotal(println(s"Hello, $name, do u want to connect to ${dbConfig.url}"))
                )
    } yield ()

}
