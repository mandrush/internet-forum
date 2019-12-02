package domain.rejection

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.ExceptionHandler
import org.postgresql.util.PSQLException

object ExceptionHandlers {

  val databaseExceptionHandler = ExceptionHandler {
    case e: PSQLException => complete(HttpResponse(InternalServerError, entity = e.getServerErrorMessage.toString))
  }

}
