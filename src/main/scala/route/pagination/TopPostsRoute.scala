package route.pagination

import akka.http.scaladsl.server.Route
import database.layer.DatabaseLayer
import domain.logic.ForumJSONSupport
import route.MainRoute.ContemporaryConfig
import akka.http.scaladsl.server.Directives._
import domain.PathNames._

import scala.util.{Failure, Success}

object TopPostsRoute extends ForumJSONSupport {

  def topPostsRoute(implicit dbLayer: DatabaseLayer,
                    cCfg: ContemporaryConfig): Route = {
    path(GetTopPosts) {
      parameters('limit.as[Int], 'offset.as[Int]) { (limit, offset) =>
        val actualLimit = if (limit > cCfg.maxPaginationLimit) cCfg.maxPaginationLimit else limit
        val topList = dbLayer.getTopPosts(actualLimit, offset)
        onComplete(topList) {
          case Success(list) =>
            complete(list)
          case Failure(e)    => throw e
        }
      }
    }
  }

}