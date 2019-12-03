package route.delete

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.{MalformedFormFieldRejection, Route}
import database.layer.DatabaseLayer
import domain.PathNames._
import domain.logic.{FieldsValidation, ForumJSONSupport}
import domain.request.UserRequests.Deletion
import route.MainRoute.ContemporaryConfig
import spray.json.JsValue

import scala.util.{Failure, Success}

object DeleteReplyRoute extends ForumJSONSupport with FieldsValidation {

  def deleteReplyRoute(implicit dbLayer: DatabaseLayer,
                       cCfg: ContemporaryConfig): Route = {
    path(DeleteReply) {
      parameter('reply_id.as[Long]) { replyId =>
        entity(as[JsValue]) { req =>
          if (!onlyContains(req, "secret")) {
            reject(MalformedFormFieldRejection("", s"Only secret field is allowed here"))
          } else {
            entity(as[Deletion]) { delete =>
              val maybeReply = dbLayer.findReply(replyId)
              onComplete(maybeReply) {
                case Success(reply) => reply match {
                  case Some(r) =>
                    if (delete.secret == r.secret.value) {
                      val deleteF = dbLayer.deleteReply(replyId)
                      onComplete(deleteF) {
                        case Success(_) => complete(HttpResponse(StatusCodes.OK, entity = s"Deleted reply with id = $replyId"))
                        case Failure(e) => throw e
                      }
                    } else {
                      complete(HttpResponse(StatusCodes.Unauthorized, entity = "Wrong secret"))
                    }
                  case None => complete(HttpResponse(StatusCodes.NotFound, entity = "There is no such reply."))
                }
                case Failure(e) => throw e
              }
            }
          }
        }
      }
    }
  }

}
