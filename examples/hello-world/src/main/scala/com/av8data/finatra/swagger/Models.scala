package com.av8data.finatra.swagger

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.annotations.RouteParam
import com.twitter.finatra.validation.constraints.{Max, Min}
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.AccessMode

import javax.inject.Inject
import org.joda.time.{DateTime, LocalDate}

@Schema(name="AddressModel", description="Sample address model for documentation")
case class Address(street: String, zip: String)

case class Student(firstName: String, lastName: String, gender: Gender, birthday: LocalDate, grade: Int, address: Option[Address])

case class StudentWithRoute(
  @RouteParam id: String,
  @Inject request: Request,
  @Schema(name = "first_name")firstName: String,
  @Schema(name = "last_name")lastName: String,
  gender: Gender,
  birthday: LocalDate,
  @Min(1) @Max(12) grade: Int,
  emails: Array[String],
  address: Option[Address]
)

case class StringWithRequest(
  @Inject request: Request,
  firstName: String
)

object CourseType extends Enumeration {
  val LEC, LAB = Value
}

case class Course(time: DateTime,
                  name: String,
                  @Schema(required = false, example = "[math,stem]")
                  tags: Seq[String],
                  @Schema(implementation = classOf[String], allowableValues = Array("LEC","LAB"))
                  typ: CourseType.Value,
                  @Schema(accessMode = AccessMode.READ_ONLY)
                  capacity: Int,
                  @Schema(implementation = classOf[Double], required = true)
                  cost: BigDecimal)
