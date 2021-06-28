# finatra-swagger

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/02f5a150c842471cb8415b6510ed0ab6)](https://www.codacy.com/app/jakehschwartz54/finatra-swagger?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jakehschwartz/finatra-swagger&amp;utm_campaign=Badge_Grade)

Add [Swagger UI](http://swagger.io/swagger-ui/) support to [Finatra](https://github.com/twitter/finatra) web framework.

Currently supports Scala 2.13 and major Finatra versions going back to 2.10.0 

For older versions, see the original repository by [xiaodongw](https://github.com/xiaodongw/swagger-finatra)

## Getting started

The major and minor version of the library matches the Finatra major and minor version:
````sbt
libraryDependencies += "com.jakehschwartz" %% "finatra-swagger" % "21.6.0"
````

First, create a subclass of a SwaggerModule
````scala
object SampleSwaggerModule extends SwaggerModule {
    
  @Singleton
  @Provides
  def swagger: Swagger = {
    val swagger = new Swagger()
    
    val info = new Info()
      .description("The Student / Course management API, this is a sample for swagger document generation")
      .version("1.0.1")
      .title("Student / Course Management API")
    
    swagger
      .info(info)
      .addSecurityDefinition("sampleBasic", {
        val d = new BasicAuthDefinition()
        d.setType("basic")
        d
      })
    
    swagger
  }
}
````

Then add the module to the list of modules in the Server. Don't forget to include the DocsController in the router!
````scala
object SampleApp extends HttpServer {
  override protected def modules = Seq(SampleSwaggerModule)

  override def configureHttp(router: HttpRouter) {
    router
      .add[DocsController]
      ...
  } 
}
````
Lastly, configure the endpoints using the `SwaggerRouteDSL`
````scala
class SampleController @Inject()(s: Swagger) extends SwaggerController {
  implicit protected val swagger = s

  getWithDoc("/students/:id") { o =>
    o.summary("Read the detail information about the student")
      .tag("Student")
      .routeParam[String]("id", "the student id")
      .responseWith[Student](200, "the student details")
      .responseWith(404, "the student is not found")
  } { request =>
    ...
  }
````
To see the Swagger UI, use the `/docs` endpoint. This can be overridden using the Finatra 
[flag](https://twitter.github.io/finatra/user-guide/getting-started/flags.html) "swagger.docs.endpoint"

To see the model that is JSON document that is generated, use `/swagger.json`. 

## Features

To see these in action, check out the [examples](/examples) directory
