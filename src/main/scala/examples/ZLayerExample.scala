package examples

import scala.io.StdIn.readLine
import zio.{ Has, UIO, ZIO, ZLayer }

import org.jinilover.microservice.ConfigTypes.{ DbConfig, WebServerConfig }

object ZLayerExample {
  def contrivedFunc1(): ZIO[Has[DbConfig], Throwable, Unit] =
    for {
      name <- UIO.effectTotal(readLine)
      _    <- ZIO
                .accessM[Has[DbConfig]] { dbConfig => // note the type enclosed by `Has`
                  UIO.effectTotal(println(s"Hello, $name, do u want to connect to ${dbConfig.get.url}"))
                }
    } yield ()

  contrivedFunc1().provideLayer(
    ZLayer.succeed(DbConfig("jdbc:postgresql://localhost:5432/postgres", "postgres", "password"))
  )

  def contrivedFunc2(): ZIO[Has[DbConfig] with Has[WebServerConfig], Throwable, Unit] =
    for {
      name     <- UIO.effectTotal(readLine)
      dbConfig <- ZIO.access[Has[DbConfig]](_.get)
      wsConfig <- ZIO.access[Has[WebServerConfig]](_.get)
      _        <- UIO.effectTotal(
                    println(s"Hello, $name, do u want to connect to ${dbConfig.url} and ${wsConfig.host}")
                  )
    } yield ()

  contrivedFunc2().provideLayer(
    ZLayer.succeed(DbConfig("jdbc:postgresql://localhost:5432/postgres", "postgres", "password")) ++ ZLayer
      .succeed(WebServerConfig("0.0.0.0", 8080))
  )
}
