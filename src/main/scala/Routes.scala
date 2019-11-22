import Domain.Item
import Paths.{HelloPath, ItemPath}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, concat, get, pathPrefix}
import akka.http.scaladsl.server.{Route, StandardRoute}
import akka.http.scaladsl.server.Directives._

import scala.util.Random

object Routes extends JSONSupport {

  import Domain._

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
      }
    )

  private def hello: StandardRoute = complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Say hello</h1>"))

  private def item: StandardRoute = complete(Item(1, "description"))

}
