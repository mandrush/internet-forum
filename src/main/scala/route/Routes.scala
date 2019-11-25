package route

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, concat, entity, get, path, post, _}
import akka.http.scaladsl.server.{Route, StandardRoute}
import domain.forum.Forum.{ForumPost, ForumResponse}
import domain.logic.{FieldsValidation, ForumJSONSupport}
import domain.request.UserRequests.{UserCreatePost, UserReply}
import spray.json.JsValue

import scala.util.Random

object Routes extends ForumJSONSupport with FieldsValidation {

  import domain.InMemoryDB._
  import domain.PathNames._
  import route.CompletionRoutes._

  sealed case class ContemporaryConfig(maxNick: Int = 21, maxTopic: Int = 80, maxContent: Int = 400, minLen: Int = 1)

  implicit val cCfg = ContemporaryConfig()

  import domain.logic.fields.RequestFields._
  val mainRoute: Route =
    concat(
      get {
        path(HelloPath) {
          hello
        }
      },
      post {
        path(CreatePost) {
          entity(as[UserCreatePost]) { request =>
            validateFields(Email(request.email), Nickname(request.nickname), Content(request.content)) {
              validate(checkField(Topic(request.topic), cCfg.minLen, cCfg.maxTopic),
                "Topic needs to have between 1 and 80 characters!") {
                val newPost = ForumPost(Random.nextInt(10), request.topic, request.content, request.nickname, request.email)
                posts += newPost
                successfulPost(newPost)
              }
            }
          }
        }
      },
      post {
        path(CreateReply / IntNumber) { id =>
          entity(as[JsValue]) { req =>
            if (req.asJsObject.fields.contains("topic")) reject
            else {
              entity(as[UserReply]) { reply =>
                findPostWithGivenId(id) match {
                  case Some(topicPost) =>
                    validateFields(Email(reply.email), Nickname(reply.nickname), Content(reply.content)) {
                      val responseToTopic = ForumResponse(Random.nextInt(10), topicPost, reply.content, reply.nickname, reply.email)
                      successfulResponse(responseToTopic)
                    }
                  case None => complete(StatusCodes.NotFound)
                }
              }
            }
          }

        }
      }
    )

  private def hello: StandardRoute = complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Say hello</h1>"))


}
