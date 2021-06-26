package com.twitter.finatra.http

import com.av8data.finatra.swagger.FinatraSwagger
import com.twitter.finagle.http.{Request, Response, RouteIndex, Status}
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.{OpenAPI, Operation}

/**
  * To work around the accessibility of RouteDSL, this class is in "com.twitter.finatra.http" package
  */
object SwaggerRouteDSL {
  implicit def convert(dsl: RouteDSL)(implicit openAPI: OpenAPI): SwaggerRouteDSL = new SwaggerRouteDSLWrapper(dsl)(openAPI)
}

trait SwaggerRouteDSL extends RouteDSL {
  implicit protected val openAPI: OpenAPI

  val noopCallback: Request => Response = _ => response.SimpleResponse(Status.Ok, "")

  def postWithDoc[RequestType: Manifest, ResponseType: Manifest](route: String,
                                                                 name: String = "",
                                                                 admin: Boolean = false,
                                                                 routeIndex: Option[RouteIndex] = None,
                                                                 registerOptionsRequest: Boolean = false)
                                                                (doc: Operation => Operation)
                                                                (callback: RequestType => ResponseType): Unit = {
    registerOperation(route, HttpMethod.POST)(doc)
    post(route, name, admin, routeIndex)(callback)
    if (registerOptionsRequest) {
      options(route, name, admin, routeIndex)(noopCallback)
    }
  }

  def getWithDoc[RequestType: Manifest, ResponseType: Manifest](route: String,
                                                                name: String = "",
                                                                admin: Boolean = false,
                                                                routeIndex: Option[RouteIndex] = None,
                                                                registerOptionsRequest: Boolean = false)
                                                               (doc: Operation => Operation)
                                                               (callback: RequestType => ResponseType): Unit = {
    registerOperation(route, HttpMethod.GET)(doc)
    get(route, name, admin, routeIndex)(callback)
    if (registerOptionsRequest) {
      options(route, name, admin, routeIndex)(noopCallback)
    }
  }

  def putWithDoc[RequestType: Manifest, ResponseType: Manifest](route: String,
                                                                name: String = "",
                                                                admin: Boolean = false,
                                                                routeIndex: Option[RouteIndex] = None,
                                                                registerOptionsRequest: Boolean = false)
                                                               (doc: Operation => Operation)
                                                               (callback: RequestType => ResponseType): Unit = {
    registerOperation(route, HttpMethod.PUT)(doc)
    put(route, name, admin, routeIndex)(callback)
    if (registerOptionsRequest) {
      options(route, name, admin, routeIndex)(noopCallback)
    }
  }

  def patchWithDoc[RequestType: Manifest, ResponseType: Manifest](route: String,
                                                                  name: String = "",
                                                                  admin: Boolean = false,
                                                                  routeIndex: Option[RouteIndex] = None,
                                                                  registerOptionsRequest: Boolean = false)
                                                                 (doc: Operation => Operation)
                                                                 (callback: RequestType => ResponseType): Unit = {
    registerOperation(route, HttpMethod.PATCH)(doc)
    patch(route, name, admin, routeIndex)(callback)
    if (registerOptionsRequest) {
      options(route, name, admin, routeIndex)(noopCallback)
    }
  }

  def headWithDoc[RequestType: Manifest, ResponseType: Manifest](route: String,
                                                                 name: String = "",
                                                                 admin: Boolean = false,
                                                                 routeIndex: Option[RouteIndex] = None,
                                                                 registerOptionsRequest: Boolean = false)
                                                                (doc: Operation => Operation)
                                                                (callback: RequestType => ResponseType): Unit = {
    registerOperation(route, HttpMethod.HEAD)(doc)
    head(route, name, admin, routeIndex)(callback)
    if (registerOptionsRequest) {
      options(route, name, admin, routeIndex)(noopCallback)
    }
  }

  def deleteWithDoc[RequestType: Manifest, ResponseType: Manifest](route: String,
                                                                   name: String = "",
                                                                   admin: Boolean = false,
                                                                   routeIndex: Option[RouteIndex] = None,
                                                                   registerOptionsRequest: Boolean = false)
                                                                  (doc: Operation => Operation)
                                                                  (callback: RequestType => ResponseType): Unit = {
    registerOperation(route, HttpMethod.DELETE)(doc)
    delete(route, name, admin, routeIndex)(callback)
    if (registerOptionsRequest) {
      options(route, name, admin, routeIndex)(noopCallback)
    }
  }

  def optionsWithDoc[RequestType: Manifest, ResponseType: Manifest](route: String,
                                                                    name: String = "",
                                                                    admin: Boolean = false,
                                                                    routeIndex: Option[RouteIndex] = None)
                                                                   (doc: Operation => Operation)
                                                                   (callback: RequestType => ResponseType): Unit = {
    registerOperation(route, HttpMethod.OPTIONS)(doc)
    options(route, name, admin, routeIndex)(callback)
  }

  private def registerOperation(path: String, method: HttpMethod)(doc: Operation => Operation): Unit = {
    FinatraSwagger
      .convert(openAPI)
      .registerOperation(prefixRoute(path), method, doc(new Operation))
  }


  //exact copy from Finatra RouteDSL class (it is defined as private there)
  private def prefixRoute(route: String): String = {
    contextWrapper {
      contextVar().prefix match {
        case prefix if prefix.nonEmpty && prefix.startsWith("/") => s"$prefix$route"
        case prefix if prefix.nonEmpty && !prefix.startsWith("/") => s"/$prefix$route"
        case _ => route
      }
    }
  }
}

private class SwaggerRouteDSLWrapper(protected val dsl: RouteDSL)(implicit protected val openAPI: OpenAPI) extends SwaggerRouteDSL {
  override private[http] val routeBuilders                 = dsl.routeBuilders
  override private[http] val annotations                   = dsl.annotations
  override private[http] lazy val contextVar               = dsl.contextVar
  override private[http] val context                       = dsl.context
  override private[http] def contextWrapper[T](f: => T): T = withContext(context)(f)
}
