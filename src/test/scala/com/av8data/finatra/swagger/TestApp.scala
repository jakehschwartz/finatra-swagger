package com.av8data.finatra.swagger

import com.google.inject.Module
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.CommonFilters
import com.twitter.finatra.http.routing.HttpRouter

object SampleAppMain extends SampleApp

class SampleApp extends HttpServer {

  override val name: String = "TestApp"
  override protected def modules: Seq[Module] = Seq(TestSwaggerModule)

  override def configureHttp(router: HttpRouter) {
    router
      .filter[CommonFilters]
      .add[DocsController]
  }
}
