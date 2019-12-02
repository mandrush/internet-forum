package route

import java.time.Instant

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, concat, entity, get, path, post, _}
import akka.http.scaladsl.server.{MalformedFormFieldRejection, Route, StandardRoute}
import database.layer.DatabaseLayer
import database.schema.FieldsValueClasses._
import database.schema.{ForumPost, ForumReply}
import domain.logic.{FieldsValidation, ForumJSONSupport, SecretGenerator}
import domain.request.UserRequests.{UserCreatePost, UserReply}
import slick.jdbc.PostgresProfile
import spray.json.JsValue

import scala.util.{Failure, Success}

object Routes extends
  ForumJSONSupport with
  FieldsValidation with
  SecretGenerator {

  import domain.PathNames._

  sealed case class ContemporaryConfig(maxNick: Int = 21, maxTopic: Int = 80, maxContent: Int = 400, minLen: Int = 1)

  implicit val cCfg = ContemporaryConfig()

  val dbLayer = new DatabaseLayer(PostgresProfile)

  import domain.rejection.ExceptionHandlers._

  val mainRoute: Route =
    concat(
      get {
        path(HelloPath) {
          hello
        }
      },
      post {
        path(CreatePost) {
          entity(as[JsValue]) { req =>
            if (!onlyContains(req, "topic", "content", "nickname", "email")) {
              reject(MalformedFormFieldRejection("", "Only topic, content, nickname and email fields are allowed"))
            } else {
              entity(as[UserCreatePost]) { request =>
                validateFields(request.email, Nickname(request.nickname), Content(request.content)) {
                  validate(checkField(Topic(request.topic), cCfg.minLen, cCfg.maxTopic),
                    "Topic needs to have between 1 and 80 characters!") {
                    val newPost = ForumPost(Topic(request.topic),
                      Content(request.content),
                      Nickname(request.nickname),
                      request.email,
                      newSecret,
                      Instant.now
                    )
                    val saved = dbLayer.exec(dbLayer.insertNewPost(newPost))
                    handleExceptions(databaseExceptionHandler) {
                      onComplete(saved) {
                        case Success(_) => complete(newPost)
                        case Failure(e) => throw e
                      }
                    }
                  }
                }
              }
            }
          }
        }
      },
      post {
        path(CreateReply) {
          parameter("topic") { topic =>
            entity(as[JsValue]) { req =>
              if (!onlyContains(req, "nickname", "content", "email")) {
                reject(MalformedFormFieldRejection("", s"Only content, nickname and email fields are allowed here"))
              }
              else {
                entity(as[UserReply]) { reply =>
                  val maybePost = dbLayer.exec(dbLayer.findPostWithTopic(topic))
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
                        case None => complete(HttpResponse(StatusCodes.NotFound))
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
    )

  private def hello: StandardRoute = complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Say hello</h1>"))

  private def onlyContains(req: JsValue, fields: String*): Boolean = req.asJsObject.fields.keySet equals fields.toSet
}
