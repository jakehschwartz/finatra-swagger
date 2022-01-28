package com.jakehschwartz.finatra.swagger

import com.google.inject.{Inject => GInject}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.annotations.{QueryParam, RouteParam, Header => HeaderParam}
import com.twitter.finatra.validation.constraints
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models._
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters._
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.`type`.TypeDescription
import net.bytebuddy.description.modifier.Visibility

import java.lang.annotation.Annotation
import java.lang.reflect.ParameterizedType
import javax.inject.{Inject => JInject}
import scala.jdk.CollectionConverters._
import scala.reflect.runtime._
import scala.reflect.runtime.universe._

object FinatraSwagger {
  private val finatraRouteParameter = ":(\\w+)".r

  private val finatraAnnotations: Set[Class[_ <: Annotation]] =
    Set(
      classOf[RouteParam],
      classOf[QueryParam],
      classOf[JInject],
      classOf[GInject],
      classOf[HeaderParam],
      classOf[constraints.AssertFalse],
      classOf[constraints.AssertTrue],
      classOf[constraints.CountryCode],
      classOf[constraints.FutureTime],
      classOf[constraints.Min],
      classOf[constraints.Max],
      classOf[constraints.NotEmpty],
      classOf[constraints.OneOf],
      classOf[constraints.PastTime],
      classOf[constraints.Pattern],
      classOf[constraints.Range],
      classOf[constraints.Size],
      classOf[constraints.TimeGranularity],
      classOf[constraints.UUID]
    )

  implicit def convert(openAPI: OpenAPI): FinatraSwagger = new FinatraSwagger(openAPI)
}

sealed trait ModelParam {
  val name: String
  val description: String
  val required: Boolean
  val typ: Class[_]
}

sealed trait FinatraRequestParam
case class RouteRequestParam(name: String, typ: Class[_], description: String = "", required: Boolean = true) extends FinatraRequestParam with ModelParam
case class QueryRequestParam(name: String, typ: Class[_], description: String = "", required: Boolean = true) extends FinatraRequestParam with ModelParam
case class HeaderRequestParam(name: String, required: Boolean = true, description: String = "", typ: Class[_]) extends FinatraRequestParam with ModelParam
case class BodyRequestParam(description: String = "", name: String, typ: Class[_], innerOptionType: Option[java.lang.reflect.Type] = None) extends FinatraRequestParam
case class RequestInjectRequestParam(name: String) extends FinatraRequestParam

class FinatraSwagger(val openAPI: OpenAPI) {

  /**
   * Register a request object that contains body information/route information/etc
   *
   * @tparam T
   * @return
   */
  def register[T: TypeTag]: List[Parameter] = {
    val properties = getFinatraProps[T]

    val swaggerFinatraProps =
      properties.collect {
        case x: ModelParam => x
      }.map {
        case param @ (_: RouteRequestParam) =>
          new PathParameter().
            name(param.name).
            description(param.description).
            required(param.required).
            schema(registerModel(param.typ))
        case param @ (_: QueryRequestParam) =>
          new QueryParameter().
            name(param.name).
            description(param.description).
            required(param.required).
            schema(registerModel(param.typ))
        case param @ (_: HeaderRequestParam) =>
          new HeaderParameter().
            name(param.name).
            description(param.description).
            required(param.required).
            schema(registerModel(param.typ))
      }

    swaggerFinatraProps ++ List(getSwaggerBodyProp[T])
  }

  private def getSwaggerBodyProp[T: TypeTag]: Parameter = {
    val clazz = currentMirror.runtimeClass(typeOf[T])

    val fields = TypeDescription.ForLoadedType
      .of(clazz)
      .getDeclaredFields
      .asScala
      .toList
    val annotations = clazz
      .getConstructors
      .head
      .getParameters
      .map(parameter => parameter.getAnnotations)
      .toList

    val bodyFieldsWithAnnotations = fields.zip(annotations)
      .filter { fieldWithAnnotations =>
        val (_, annotations) = fieldWithAnnotations
        val doesNotContainFinatraAnnotations = FinatraSwagger.finatraAnnotations
          .intersect(annotations.map(_.annotationType()).toSet)
          .isEmpty

        doesNotContainFinatraAnnotations
      }

    val dynamicType = new ByteBuddy()
      .subclass(classOf[Object])
      .name("swagger." + clazz.getCanonicalName)

    val populatedType = bodyFieldsWithAnnotations.foldLeft(dynamicType) { (asm, fieldWithAnnotations) =>
      val (field, annotations) = fieldWithAnnotations
      asm
        .defineField(field.getName, field.getType, Visibility.PUBLIC)
        .annotateField(annotations.toList.asJava)
    }

    val bodyProperty = registerModel(populatedType.make.load(getClass.getClassLoader).getLoaded)

    new Parameter()
      .name("body")
      .schema(bodyProperty)
  }

  /**
   * Given the request object format its finatra parameters via reflection
   *
   * @tparam T
   * @return
   */
  private def getFinatraProps[T: TypeTag]: List[FinatraRequestParam] = {
    val clazz = currentMirror.runtimeClass(typeOf[T])

    val fields = clazz.getDeclaredFields

    val constructorArgWithField =
      clazz.
        getConstructors.
        head.getParameters.
        map(m => (clazz: Class[_ <: Annotation]) => {
          val annotation = m.getAnnotationsByType(clazz)

          if (annotation.isEmpty) {
            None
          } else {
            Some(annotation)
          }
        }).
        zip(fields)

    val ast: List[Option[FinatraRequestParam]] =
      constructorArgWithField.map { case (annotationExtractor, field) =>
        val routeParam = annotationExtractor(classOf[RouteParam])
        val queryParam = annotationExtractor(classOf[QueryParam])
        val injectJavax = annotationExtractor(classOf[JInject])
        val injectGuice = annotationExtractor(classOf[GInject])
        val header = annotationExtractor(classOf[HeaderParam])

        val (isRequired, innerOptionType) = field.getGenericType match {
          case parameterizedType: ParameterizedType =>

            val required = parameterizedType.getRawType.asInstanceOf[Class[_]] == classOf[Option[_]]

            (required, Some(parameterizedType.getActualTypeArguments.apply(0)))
          case _ =>
            (true, None)
        }

        if (routeParam.isDefined) {
          Some(RouteRequestParam(field.getName, typ = field.getType))
        }
        else if (queryParam.isDefined) {
          Some(QueryRequestParam(field.getName, typ = field.getType, required = isRequired))
        }
        else if ((injectJavax.isDefined || injectGuice.isDefined) && field.getType.isAssignableFrom(classOf[Request])) {
          Some(RequestInjectRequestParam(field.getName))
        }
        else if (header.isDefined) {
          Some(HeaderRequestParam(field.getName, typ = field.getType, required = isRequired))
        }
        else {
          Some(BodyRequestParam(name = field.getName, typ = field.getType, innerOptionType = innerOptionType))
        }
      }.toList

    ast.flatten
  }

  def registerModel[T: TypeTag]: Schema[_] = {
    val paramType: Type = typeOf[T]
    if (paramType =:= TypeTag.Nothing.tpe) {
      null
    } else {
      val typeClass = currentMirror.runtimeClass(paramType)

      registerModel(typeClass)
    }
  }

  private def registerModel(typeClass: Class[_]): Schema[_] = {
    val modelConverters = ModelConverters.getInstance()
    val models = modelConverters.readAll(typeClass)
    for (entry <- models.entrySet().asScala) {
      openAPI.schema(entry.getKey, entry.getValue)
    }
    models.get(typeClass.getSimpleName)
  }

  def convertPath(path: String): String = {
    FinatraSwagger.finatraRouteParameter.replaceAllIn(path, "{$1}")
  }

  def registerOperation(path: String, method: HttpMethod, operation: Operation): OpenAPI = {
    val swaggerPath = convertPath(path)

    val spath = if (openAPI.getPaths == null || openAPI.getPaths.get(swaggerPath) == null) {
      new PathItem()
    } else {
      openAPI.getPaths.get(swaggerPath)
    }
    spath.operation(method, operation)
    openAPI.path(swaggerPath, spath)
  }
}
