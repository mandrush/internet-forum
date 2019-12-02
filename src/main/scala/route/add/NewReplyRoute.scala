package route.add

import java.time.Instant

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{MalformedFormFieldRejection, Route}
import database.layer.DatabaseLayer
import database.schema.FieldsValueClasses.{Content, Nickname}
import database.schema.ForumReply
import domain.PathNames.CreateReply
import domain.logic.{FieldsValidation, ForumJSONSupport, SecretGenerator}
import domain.rejection.ExceptionHandlers.databaseExceptionHandler
import domain.request.UserRequests.UserReply
import route.MainRoute.ContemporaryConfig
import spray.json.JsValue
import akka.http.scaladsl.server.Directives._
import scala.util.{Failure, Success}

object NewReplyRoute extends ForumJSONSupport
  with FieldsValidation
  with SecretGenerator {

  def newReplyRoute(implicit dbLayer: DatabaseLayer,
                    cCfg: ContemporaryConfig): Route =
    path(CreateReply) {
      parameter('post_id.as[Long]) { postId =>
        entity(as[JsValue]) { req =>
          if (!onlyContains(req, "nickname", "content", "email")) {
            reject(MalformedFormFieldRejection("", s"Only content, nickname and email fields are allowed here"))
          }
          else {
            entity(as[UserReply]) { reply =>
              validateFields(reply.email, Nickname(reply.nickname), Content(reply.content)) {
                val maybePost = dbLayer.exec(dbLayer.findPost(postId))
                handleExceptions(databaseExceptionHandler) {
                  onComplete(maybePost) {
                    case Success(p) => p match {
                      case Some(found) =>
                        val newReply = ForumReply(Content(reply.content),
                          Nickname(reply.nickname),
                          reply.email,
                          Instant.now,
                          newSecret,
                          found.id)
                        val saved = dbLayer.exec(dbLayer.insertNewReply(newReply))
                        onComplete(saved) {
                          case Success(_) => complete(newReply)
                          case Failure(e) => throw e
                        }
                      case None => complete(HttpResponse(StatusCodes.NotFound, entity = "There is no such post."))
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

}
