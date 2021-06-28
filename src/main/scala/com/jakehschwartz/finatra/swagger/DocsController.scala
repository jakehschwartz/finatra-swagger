package com.jakehschwartz.finatra.swagger

import java.io.BufferedInputStream
import java.util.Date
import com.twitter.finagle.http.{Message, Request}
import com.twitter.finatra.http.Controller
import com.twitter.util.jackson.ScalaObjectMapper
import com.twitter.inject.annotations.Flag
import io.swagger.v3.oas.models.OpenAPI

import javax.activation.MimetypesFileTypeMap
import javax.inject.{Inject, Singleton}
import org.apache.commons.io.FilenameUtils
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

import scala.util.{Failure, Success, Try}

@Singleton
class DocsController @Inject()(openAPI: OpenAPI,
                               objectMapper: ScalaObjectMapper,
                               @Flag("swagger.docs.endpoint") endpoint: String)
    extends Controller {

  get("/swagger.json") { _: Request =>
    response
      .ok(objectMapper.writeValueAsString(openAPI))
      .contentTypeJson
  }

  get(s"$endpoint") { _: Request =>
    response.temporaryRedirect
      .location(s"$endpoint/swagger-ui/${BuildInfo.swaggerUIVersion}/index.html?url=/swagger.json")
  }

  private val defaultExpireTimeMillis: Long = 86400000L // 1 day
  private val extMap = new MimetypesFileTypeMap()

  get(s"$endpoint/:*") { request: Request =>
    val resourcePath = request.getParam("*")

    val webjarsResourceURI: String = s"/META-INF/resources/webjars/$resourcePath"
    if (isDirectoryRequest(webjarsResourceURI)) {
      response.forbidden
    } else {
      val eTagNameTry = Try(getETagName(webjarsResourceURI))
      eTagNameTry match {
        case Failure(_) => response.notFound
        case Success(eTagName) =>
          if (checkETagMatch(request, eTagName) || checkLastModify(request)) {
            response.notModified
          } else {
            val inputStream = getClass.getResourceAsStream(webjarsResourceURI)
            if (inputStream != null) {
              try {
                val filename = getFileName(webjarsResourceURI)
                val body = new BufferedInputStream(inputStream)

                response.ok
                  .body(body)
                  .header("ETag", eTagName)
                  .header(
                    "Expires",
                    Message.httpDateFormat(new Date(
                      System.currentTimeMillis() + defaultExpireTimeMillis)))
                  .header("Last-Modified",
                          Message.httpDateFormat(
                            new Date(System.currentTimeMillis())))
                  .header(
                    "Cache-Control",
                    s"max-age=${defaultExpireTimeMillis / 1000}, must-revalidate")
                  .contentType(getContentType(filename))
              } finally {
                inputStream.close()
              }
            } else response.notFound
          }
      }
    }
  }

  private def isDirectoryRequest(uri: String): Boolean = {
    uri.endsWith("/")
  }

  private def getFileName(webjarsResourceURI: String): String = {
    val tokens: Array[String] = webjarsResourceURI.split("/")
    tokens(tokens.length - 1)
  }

  private def getETagName(webjarsResourceURI: String): String = {
    val tokens: Array[String] = webjarsResourceURI.split("/")
    if (tokens.length < 7) {
      throw new IllegalArgumentException(
        "insufficient URL has given: " + webjarsResourceURI)
    }
    val version: String = tokens(5)
    val fileName: String = tokens(tokens.length - 1)

    fileName + "_" + version
  }

  private def checkETagMatch(request: Request, eTagName: String): Boolean = {
    request.headerMap.get("If-None-Match") match {
      case None        => false
      case Some(token) => token == eTagName
    }
  }

  val dateParser: DateTimeFormatter =
    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss zzz")
  private def checkLastModify(request: Request): Boolean = {
    request.headerMap
      .get("If-Modified-Since")
      .map(dateParser.parseDateTime)
      .map(_.getMillis) match {
      case None       => false
      case Some(last) => last - System.currentTimeMillis > 0L
    }
  }

  private def getContentType(file: String) = {
    extMap.getContentType(dottedFileExtension(file))
  }

  private def dottedFileExtension(uri: String) = {
    s".${FilenameUtils.getExtension(uri)}"
  }
}
