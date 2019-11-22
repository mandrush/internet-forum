package route

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, concat, get, path, post, _}
import akka.http.scaladsl.server.{Route, StandardRoute}
import domain.JSONSupport
import route.logic.ForumPostValidation
// for JSON serialization/deserialization following dependency is required:
// "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7"
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
      get {
        path(ItemPath) {
          item
        }
      },
      post {
        path(CreatePost) {
          entity(as[ForumPost]) { post =>
            validate(validateEmail(post.email), "Email was incorrect or not specified") {
              posts += post
              complete(post)
            }

          }
        }
      }
    )

  private def hello: StandardRoute = complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Say hello</h1>"))

  private def item: StandardRoute = complete(Item(1, "description"))

}
