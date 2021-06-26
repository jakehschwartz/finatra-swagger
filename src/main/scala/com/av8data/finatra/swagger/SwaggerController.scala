package com.av8data.finatra.swagger

import com.twitter.finatra.http.{Controller, SwaggerRouteDSL}

trait SwaggerController extends Controller with SwaggerRouteDSL {
  self: Controller =>

  implicit protected val convertToFinatraOperation = FinatraOperation.convert _
  implicit protected val convertToFinatraSwagger = FinatraSwagger.convert _
  implicit protected val convertToSwaggerRouteDSL = SwaggerRouteDSL.convert _
}
