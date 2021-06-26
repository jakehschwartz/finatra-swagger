package com.av8data.finatra.swagger

import com.twitter.conversions.DurationOps._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.finatra.http.EmbeddedHttpServer

class DocsControllerTest extends FeatureTestBase {

  override lazy val server: EmbeddedHttpServer = makeServer(
    serverName = "docsControllerServer")

  private val swaggerUrl: String =
    s"/docs/swagger-ui/${BuildInfo.swaggerUIVersion}/index.html?url=/swagger.json"

  test("sampleController: docs endpoint should return 307") {
    val expectedLocation: String = swaggerUrl
    val response: Response =
      server.httpGet("/docs", andExpect = Status.TemporaryRedirect)
    response.headerMap("Location") shouldBe expectedLocation
  }

  test("sampleController: docs endpoint should return 200 from full URL") {
    server.httpGet(swaggerUrl, andExpect = Status.Ok)
  }

  test("sampleController: /swagger.json should be returned") {
    server.httpGet("/swagger.json", andExpect = Status.Ok)
  }

  test("Startup and be healthy") {
    server.assertStarted()
    server.assertHealthy()
    server.close(20.seconds)
    server.assertCleanShutdown()
  }
}
