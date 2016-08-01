package org.http4s.client.testroutes

import scalaz.stream.Process

import org.http4s.Status._
import org.http4s.{Response, TransferCoding}

trait GetRoutes {

  /////////////// Test routes for clients ////////////////////////////////

  protected val getPaths: Map[String, Response] = {
    import org.http4s.headers._
    Map(
      "/simple" -> Response(Ok).withBody("simple path").run,
      "/chunked" -> Response(Ok).withBody(Process.emit("chunk1")).map(_.putHeaders(`Transfer-Encoding`(TransferCoding.chunked))).run
    )
  }

}
