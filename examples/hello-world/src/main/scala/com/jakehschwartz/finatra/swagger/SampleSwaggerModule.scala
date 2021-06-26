package com.av8data.finatra.swagger

import com.google.inject.Provides
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement

import javax.inject.Singleton

object SampleSwaggerModule extends SwaggerModule {

  @Singleton
  @Provides
  def openAPI: OpenAPI = {
    val openAPI = new OpenAPI()

    val info = new Info()
      .description(
        "The Student / Course management API, this is a sample for swagger document generation")
      .version("1.0.1")
      .title("Student / Course Management API")

    openAPI
      .info(info)
      .addSecurityItem(new SecurityRequirement().addList("sampleBasic", "basic"))

    openAPI
  }
}
