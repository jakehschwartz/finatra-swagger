package com.av8data.finatra.swagger

import java.util.Date
import javax.inject.Inject
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import io.swagger.v3.oas.models.OpenAPI
import org.joda.time.{DateTime, LocalDate}

class SampleFilter extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    service(request)
  }
}

class SampleController @Inject()(implicit val openAPI: OpenAPI) extends SwaggerController {

  case class HelloResponse(text: String, time: Date)

  getWithDoc("/students/:id", registerOptionsRequest = true) { o =>
    o.summary("Read student information")
      .description("Read the detail information about the student.")
      .addTagsItem("Student")
      .pathParam[String]("id", "the student id")
      .responseWith[Student](200, "the student object", "application/json",
      example = Some(Student("Tom", "Wang", Gender.Male, new LocalDate(), 4, Some(Address("California Street", "94111")))))
      .responseWith(404, "the student is not found")
  } { request: Request =>
    val id = request.getParam("id")

    response.ok.json(Student("Alice", "Wang", Gender.Female, new LocalDate(), 4, Some(Address("California Street", "94111")))).toFuture
  }

  postWithDoc("/students/:id") { o =>
    o.summary("Sample request with route")
      .description("Read the detail information about the student.")
      .addTagsItem("Student")
      .request[StudentWithRoute]
  } { request: StudentWithRoute =>
    val id = request.id

    response.ok.json(Student("Alice", "Wang", Gender.Female, new LocalDate(), 4, Some(Address("California Street", "94111")))).toFuture
  }

  postWithDoc("/students/test/:id", registerOptionsRequest = true) { o =>
    o.summary("Sample request with route2")
      .description("Read the detail information about the student.")
      .addTagsItem("Student")
      .request[StudentWithRoute]
  } { request: StudentWithRoute =>
    val id = request.id

    response.ok.json(Student("Alice", "Wang", Gender.Female, new LocalDate(), 4, Some(Address("California Street", "94111")))).toFuture
  }

  postWithDoc("/students/firstName", registerOptionsRequest = true) {
    _.request[StringWithRequest]
      .addTagsItem("Student")
  } { request: StringWithRequest =>
    request.firstName
  }

  postWithDoc("/students") { o =>
    o.summary("Create a new student")
      .addTagsItem("Student")
      .bodyParam[Student]("the student details")
      .responseWith[Student](200, "the student is created")
      .responseWith(500, "internal error")
  } { student: Student =>
    //val student = request.contentString
    response.ok.json(student).toFuture
  }

  postWithDoc("/students/bulk", registerOptionsRequest = true) { o =>
    o.summary("Create a list of students")
      .addTagsItem("Student")
      .bodyParam[Array[Student]]("the list of students")
      .responseWith[List[Student]](200, "the students are created")
      .responseWith(500, "internal error")
  } { students: List[Student] =>
    response.ok.json(students).toFuture
  }

  putWithDoc("/students/:id") { o =>
    o.summary("Update the student")
      .addTagsItem("Student")
      .pathParam[String]("id", "student ID")
      .cookieParam[String]("who", "who make the update")
      .headerParam[String]("token", "the token")
      .responseWith(200, "the student is updated")
      .responseWith(404, "the student is not found")
  } { request: Request =>
    val grade = request.getIntParam("grade")
    val who = request.cookies.getOrElse("who", "Sam") //todo swagger-ui not set the cookie?
    val token = request.headerMap("token")

    response.ok.toFuture
  }

  getWithDoc("/students", registerOptionsRequest = true) { o =>
    o.summary("Get a list of students")
      .addTagsItem("Student")
      .responseWith[Array[String]](200, "the student ids")
      .addSecurity("sampleBasic", List())
  } { request: Request =>
    response.ok.json(Array("student1", "student2")).toFuture
  }

  getWithDoc("/courses", registerOptionsRequest = true) { o =>
    o.summary("Get a list of courses")
      .addTagsItem("Course")
      .responseWith[Array[String]](200, "the courses ids")
      .responseWith(500, "internal error")
  } { request: Request =>
    response.ok.json(Array("course1", "course2")).toFuture
  }

  getWithDoc("/courses/:id", registerOptionsRequest = true) { o =>
    o.summary("Get the detail of a course")
      .addTagsItem("Course")
      .pathParam[String]("id", "the course id")
      .responseWith[Course](200, "the courses detail")
      .responseWith(500, "internal error")
  } { request: Request =>
    response.ok.json(Course(new DateTime(), "calculation", Seq("math"), CourseType.LAB, 20, BigDecimal(300.54))).toFuture
  }

  filter[SampleFilter].getWithDoc("/courses/:courseId/student/:studentId", registerOptionsRequest = true) { o =>
    o.summary("Is the student in this course")
      .tags(List("Course", "Student"))
      .pathParam[String]("courseId", "the course id")
      .pathParam[String]("studentId", "the student id")
      .responseWith[Boolean](200, "true / false")
      .responseWith(500, "internal error")
      .deprecated(true)
  } { request: Request =>
    response.ok.json(true).toFuture
  }


}
