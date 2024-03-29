package route.edit

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{MalformedFormFieldRejection, Route}
import database.layer.DatabaseLayer
import database.schema.FieldsValueClasses.Content
import database.schema.ForumPost
import domain.PathNames._
import domain.logic.{FieldsValidation, ForumJSONSupport}
import domain.rejection.ExceptionHandlers.databaseExceptionHandler
import domain.request.UserRequests.UserEdit
import route.AppConfig
import spray.json.JsValue

import scala.util.{Failure, Success}

object EditPostRoute extends ForumJSONSupport with FieldsValidation {

  def editPostRoute(implicit dbLayer: DatabaseLayer,
                    cCfg: AppConfig): Route = {
    path(EditPost) {
      parameter('post_id.as[Long]) { postId =>
        entity(as[JsValue]) { req =>
          if (!onlyContains(req, "newContent", "secret")) {
            reject(MalformedFormFieldRejection("", s"""For edits only "newContent" and "secret" fields are allowed"""))
          } else {
            entity(as[UserEdit]) { entity =>
              validateField(Content(entity.newContent), cCfg.minimumLength, cCfg.maxContentLength) {
                val maybePost = dbLayer.findPost(postId)
                handleExceptions(databaseExceptionHandler) {
                  onComplete(maybePost) {
                    case Success(post) => post match {
                      case Some(p) =>
                        if (p.secret.value == entity.secret) {
                          val update = dbLayer.updatePostContent(postId, entity.newContent)
                          onComplete(update) {
                            case Success(_) =>
                              val updated = ForumPost(p.topic, Content(entity.newContent), p.nickname, p.email, p.secret, p.createTs, p.createTs)
                              complete(updated)
                            case Failure(e) => throw e
                          }
                        } else {
                          complete(HttpResponse(StatusCodes.Unauthorized, entity = "Wrong secret"))
                        }
                      case None    => complete(HttpResponse(StatusCodes.NotFound, entity = "There is no such post."))
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
