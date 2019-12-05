package route

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, concat, get, path, post, _}
import akka.http.scaladsl.server.{Route, StandardRoute}
import database.layer.DatabaseLayer
import slick.jdbc.PostgresProfile

object MainRoute {

  import domain.PathNames._

  sealed case class ContemporaryConfig(maxNick: Int = 21, maxTopic: Int = 80, maxContent: Int = 400, minLen: Int = 1)

  implicit val cCfg = ContemporaryConfig()

  implicit val dbLayer = new DatabaseLayer(PostgresProfile)

  import route.add.NewPostRoute._
  import route.add.NewReplyRoute._
  import route.delete.DeletePostRoute._
  import route.delete.DeleteReplyRoute._
  import route.edit.EditPostRoute._
  import route.edit.EditReplyRoute._

  val mainRoute: Route =
    concat(
      get {
        path(HelloPath) {
          hello
        }
      },
      post {
        newPostRoute
      },
      post {
        newReplyRoute
      },
      post {
        editPostRoute
      },
      post {
        editReplyRoute
      },
      post {
        deletePostRoute
      },
      post {
        deleteReplyRoute
      }
    )

  private def hello: StandardRoute = complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Say hello</h1>"))

}
