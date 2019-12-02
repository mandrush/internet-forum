package domain.rejection

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

object Rejections {


  implicit def rejectionHandler = RejectionHandler.newBuilder()
    .handle {
      case MalformedFormFieldRejection(_, errorMsg, _) =>
        complete(HttpResponse(BadRequest, entity = errorMsg))
    }
    .handleAll[MethodRejection] { methodRejections =>
      val names = methodRejections.map(_.supported.name)
      complete((MethodNotAllowed, s"Can't do that! Supported: ${names mkString " or "}!"))
    }
    .result()

}
