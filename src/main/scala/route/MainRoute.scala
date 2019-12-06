package route

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{concat, get, post}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import database.layer.DatabaseLayer
import logger.StandaloneLogger

class MainRoute(implicit appCfg: AppConfig, dbLayer: DatabaseLayer) extends StandaloneLogger {

  import route.add.NewPostRoute._
  import route.add.NewReplyRoute._
  import route.delete.DeletePostRoute._
  import route.delete.DeleteReplyRoute._
  import route.edit.EditPostRoute._
  import route.edit.EditReplyRoute._
  import route.pagination.TopPostsRoute._
  import route.pagination.RepliesPaginationRoute._

  def setupRouting(host: String, port: Int)
                  (implicit system: ActorSystem, materializer: ActorMaterializer) = Http().bindAndHandle(mainRoute, host, port)

  val mainRoute: Route =
    concat(
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
      },
      get {
        topPostsRoute
      },
      get {
        repliesPaginatedRoute
      }
    )

}
