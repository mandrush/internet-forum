package route

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, concat, get, path, post, _}
import akka.http.scaladsl.server.{Route, StandardRoute}
import domain.JSONSupport
import domain.logic.ForumPostValidation

object Routes extends JSONSupport with ForumPostValidation {

  import domain.Domain._
  import domain.Paths._
  import domain.InMemoryDB._

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
            validateForumPost(post) {
              successfulPost(post)
            }
          }
        }
      }
    )

  private def hello: StandardRoute = complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Say hello</h1>"))


}
