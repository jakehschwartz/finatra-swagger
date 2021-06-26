package com.av8data.finatra.swagger

import io.swagger.v3.oas.models._
import io.swagger.v3.oas.models.media.{Content, MediaType}
import io.swagger.v3.oas.models.parameters._
import io.swagger.v3.oas.models.responses.{ApiResponse, ApiResponses}
import io.swagger.v3.oas.models.security.SecurityRequirement

import scala.jdk.CollectionConverters._
import scala.reflect.runtime.universe._

object FinatraOperation {
  implicit def convert(operation: Operation): FinatraOperation = new FinatraOperation(operation)
}

class FinatraOperation(operation: Operation) {
  import FinatraSwagger._

  def pathParam[T: TypeTag](name: String, description: String = "", required: Boolean = true)
                           (implicit openAPI: OpenAPI): Operation = {
    val param = new PathParameter()
      .name(name)
      .description(description)
      .required(required)
      .schema(openAPI.registerModel[T])

    operation.addParametersItem(param)
  }

  def request[T <: Product : TypeTag](implicit openAPI: OpenAPI): Operation = {
    operation.setParameters(openAPI.register[T].asJava)

    operation
  }

  def queryParam[T: TypeTag](name: String, description: String = "", required: Boolean = true)
                            (implicit openAPI: OpenAPI): Operation = {
    val param = new QueryParameter()
      .name(name)
      .description(description)
      .required(required)
      .schema(openAPI.registerModel[T])

    operation.addParametersItem(param)
  }

  def headerParam[T: TypeTag](name: String, description: String = "", required: Boolean = true)
                             (implicit openAPI: OpenAPI): Operation = {
    val param = new HeaderParameter()
      .name(name)
      .description(description)
      .required(required)
      .schema(openAPI.registerModel[T])

    operation.addParametersItem(param)
  }

  def cookieParam[T: TypeTag](name: String, description: String = "", required: Boolean = true)
                             (implicit openAPI: OpenAPI): Operation = {
    val param = new CookieParameter()
      .name(name)
      .description(description)
      .required(required)
      .schema(openAPI.registerModel[T])

    operation.addParametersItem(param)
    operation
  }

  def bodyParam[T: TypeTag](description: String = "", example: Option[T] = None)
                           (implicit openAPI: OpenAPI): Operation = {
    val model = openAPI.registerModel[T]

    val content = new Content
    val mediaType = new MediaType()
      .schema(model)
    val mt = example.fold(mediaType)(mediaType.example)
    content.addMediaType("application/json", mt)

    val reqBody = new RequestBody()
      .content(content)
      .description(description)
    operation.requestBody(reqBody)

    operation
  }

  def responseWith[T: TypeTag](status: Int,
                               description: String = "",
                               contentType: String = "",
                               example: Option[T] = None)
                              (implicit openAPI: OpenAPI): Operation = {
    val ref = openAPI.registerModel[T]

//    //todo not working, sample is not in the generated api, waiting for swagger fix
    example.foreach { e =>
      if (ref != null) {
        ref.setExample(e)
//        //val model = api.swagger.getDefinitions.get(ref.asInstanceOf[RefProperty].getSimpleRef)
//        //model.setExample(example)
      }
    }

    val content = new Content
    val mediaType = new MediaType().schema(ref)
    content.addMediaType(contentType, example.fold(mediaType)(mediaType.example))

    val apiResponse = new ApiResponse()
      .description(description)
      .content(content)

    if (operation.getResponses == null) {
      operation.responses(new ApiResponses().addApiResponse(status.toString, apiResponse))
    } else {
      operation.getResponses.addApiResponse(status.toString, apiResponse)
      operation
    }
  }

  def addSecurity(name: String, scopes: List[String]): Operation = {
    operation.addSecurityItem(new SecurityRequirement().addList(name, scopes.asJava))
  }

  def tag(tag: String): Operation = {
    tags(List(tag))
  }

  def tags(tags: List[String]): Operation = {
    operation.setTags(tags.asJava)
    operation
  }
}
