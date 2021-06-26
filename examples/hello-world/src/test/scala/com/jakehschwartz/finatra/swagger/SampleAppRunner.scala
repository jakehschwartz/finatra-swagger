package com.av8data.finatra.swagger

import com.twitter.finagle.http.Status
import com.twitter.finatra.http.EmbeddedHttpServer

class SampleAppRunner extends SampleAppBaseTest {

  override lazy val server: EmbeddedHttpServer = makeServer(
    serverName = "sampleControllerServer")

  private val swaggerUrl: String =
    s"/docs/swagger-ui/${BuildInfo.swaggerUIVersion}/index.html?url=/swagger.json"


  test("sampleController: docs endpoint should return 200 from full URL") {
    server.httpGet(swaggerUrl, andExpect = Status.Ok)
  }

  test("sampleController: /swagger.json should be returned") {
    server.httpGet("/swagger.json", andExpect = Status.Ok)
  }

  ignore("Startup and be healthy, and run indefinitely so you can interact with it") {
    server.assertStarted()
    server.assertHealthy()
    Thread.sleep(Int.MaxValue)
  }
}
