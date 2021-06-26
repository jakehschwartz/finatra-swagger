package com.av8data.finatra.swagger

import com.google.inject.Stage
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.inject.server.FeatureTest
import org.scalatest.matchers.should.Matchers

trait FeatureTestBase extends FeatureTest with Matchers {

  override protected def beforeEach(): Unit = {
    server.assertStarted()
    server.assertHealthy()
  }

  def stage: Stage = Stage.DEVELOPMENT
  override def server: EmbeddedHttpServer

  // see https://twitter.github.io/finatra/user-guide/testing/feature_tests.html#sharing-a-server-fixture-between-many-feature-tests
  // for best practices in sharing a server fixture between tests

  protected def makeServer(serverName: String): EmbeddedHttpServer = new EmbeddedHttpServer(
    twitterServer = new SampleApp { override val name: String = serverName },
    stage = stage
  )

}
