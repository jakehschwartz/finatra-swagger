package com.av8data.finatra.swagger

import com.twitter.inject.TwitterModule

/**
  * Created by Jake on 4/26/17.
  */
abstract class SwaggerModule extends TwitterModule {
  flag(name = "swagger.docs.endpoint", default = "/docs", help = "The key to use")
}
