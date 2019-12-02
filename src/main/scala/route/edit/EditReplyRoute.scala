package route.edit

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{MalformedFormFieldRejection, Route}
import database.layer.DatabaseLayer
import database.schema.FieldsValueClasses.Content
import database.schema.ForumReply
import domain.logic.{FieldsValidation, ForumJSONSupport}
import domain.rejection.ExceptionHandlers.databaseExceptionHandler
import domain.request.UserRequests.UserEdit
import route.MainRoute.ContemporaryConfig
import spray.json.JsValue

import scala.util.{Failure, Success}

object EditReplyRoute extends ForumJSONSupport with FieldsValidation {

  import domain.PathNames._

  def editReplyRoute(implicit dbLayer: DatabaseLayer,
                     cCfg: ContemporaryConfig): Route = {
    path(EditReply) {
      parameter('reply_id.as[Long]) { replyId =>
        entity(as[JsValue]) { req =>
          if (!onlyContains(req, "newContent", "secret")) {
            reject(MalformedFormFieldRejection("", s"""For edits only "newContent" and "secret" fields are allowed"""))
          } else {
            entity(as[UserEdit]) { entity =>
              validateField(Content(entity.newContent), cCfg.minLen, cCfg.maxContent) {
                val maybeReply = dbLayer.findReply(replyId)
                handleExceptions(databaseExceptionHandler) {
                  onComplete(maybeReply) {
                    case Success(reply) => reply match {
                      case Some(r) =>
                        if (r.secret.value == entity.secret) {
                          val update = dbLayer.updateReply(replyId, entity.newContent)
                          onComplete(update) {
                            case Success(_) =>
                              val updated = ForumReply(Content(entity.newContent), r.nickname, r.email, r.timestamp, r.secret, r.parentId)
                              complete(updated)
                            case Failure(e) => throw e
                          }
                        } else {
                          complete(HttpResponse(StatusCodes.Unauthorized, entity = "Wrong secret"))
                        }
                      case None    => complete(HttpResponse(StatusCodes.NotFound, entity = "There is no such reply."))
                    }
                    case Failure(e)    => throw e
                  }
                }
              }
            }
          }
        }
      }
    }
  }


}
