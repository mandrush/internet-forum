package route

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, concat, get, path, post, _}
import akka.http.scaladsl.server.{Route, StandardRoute}
import domain.logic.{ForumJSONSupport, FieldsValidation}

object Routes extends ForumJSONSupport with FieldsValidation {

  import domain.Forum._
  import domain.PathNames._
  import domain.InMemoryDB._
  import route.CompletionRoutes._

  val mainRoute: Route =
    concat(
      get {
        path(HelloPath) {
          hello
        }
      },
      post {
        path(CreatePost) {
          entity(as[ForumPost]) { post =>
            validateFields(post.email, post.nickname, post.content) {
              validate(checkTopic(post.topic), "Topic needs to have between 1 and 80 characters!") {
                posts += post
                successfulPost(post)
              }
            }
          }
        }
      },
      post {
        path(CreateResponse / IntNumber) { id =>
          entity(as[BasicForumEntity]) { userResp =>
            findPostWithGivenId(id) match {
              case Some(topicPost) =>
                validateFields(userResp.email, userResp.nickname, userResp.content) {
                  val responseToTopic = ForumResponse(topicPost, userResp.content, userResp.nickname, userResp.email)
                  successfulResponse(responseToTopic)
                }
              case None       => complete(StatusCodes.NotFound)
            }
          }
        }
      }
    )

  private def hello: StandardRoute = complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Say hello</h1>"))


}
