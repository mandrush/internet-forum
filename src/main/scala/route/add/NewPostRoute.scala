package route.add

import java.time.Instant

import akka.http.scaladsl.server.Directives.{as, complete, entity, handleExceptions, onComplete, path, reject, validate}
import akka.http.scaladsl.server.{MalformedFormFieldRejection, Route}
import database.layer.DatabaseLayer
import database.schema.FieldsValueClasses.{Content, Nickname, Topic}
import database.schema.{ForumPost, PK}
import domain.PathNames.CreatePost
import domain.logic.{FieldsValidation, ForumJSONSupport, SecretGenerator}
import domain.rejection.ExceptionHandlers.databaseExceptionHandler
import domain.request.UserRequests.UserCreatePost
import route.MainRoute.ContemporaryConfig
import spray.json.JsValue

import scala.util.{Failure, Success}

object NewPostRoute extends ForumJSONSupport
  with FieldsValidation
  with SecretGenerator {

  def newPostRoute(implicit dbLayer: DatabaseLayer,
                   cCfg: ContemporaryConfig): Route = {
    path(CreatePost) {
      entity(as[JsValue]) { req =>
        if (!onlyContains(req, "topic", "content", "nickname", "email")) {
          reject(MalformedFormFieldRejection("", "Only topic, content, nickname and email fields are allowed"))
        } else {
          entity(as[UserCreatePost]) { request =>
            validateFields(request.email, Nickname(request.nickname), Content(request.content)) {
              validateField(Topic(request.topic), cCfg.minLen, cCfg.maxTopic) {
                val newPost = ForumPost(Topic(request.topic),
                  Content(request.content),
                  Nickname(request.nickname),
                  request.email,
                  newSecret,
                  Instant.now
                )
                val saved = dbLayer.insertNewPost(newPost)
                handleExceptions(databaseExceptionHandler) {
                  onComplete(saved) {
                    case Success(x) => complete(newPost.copy(id = PK[ForumPost](x.value)))
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
}
