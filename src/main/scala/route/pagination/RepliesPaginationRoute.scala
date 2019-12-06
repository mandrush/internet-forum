package route.pagination

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import database.layer.DatabaseLayer
import domain.logic.ForumJSONSupport
import akka.http.scaladsl.server.Directives._
import database.schema.ForumReply
import domain.PathNames._
import route.AppConfig
import domain.rejection.ExceptionHandlers.databaseExceptionHandler

import scala.util.{Failure, Success}

object RepliesPaginationRoute extends ForumJSONSupport {

  def repliesPaginatedRoute(implicit dbLayer: DatabaseLayer,
                            cCfg: AppConfig): Route = {
    path(GetPaginatedReplies) {
      parameters('post_id.as[Int], 'reply_id.as[Int], 'before.as[Int], 'after.as[Int]) { (postId, replyId, before, after) =>
        val maybePost = dbLayer.findPost(postId)
        handleExceptions(databaseExceptionHandler) {
          onComplete(maybePost) {
            case Success(p) => p match {
              case Some(_) =>
                val repliesToGivenPost = dbLayer.repliesToPost(postId)
                onComplete(repliesToGivenPost) {
                  case Success(list) =>
                    val replies = list.unzip._1
                    if (!replies.map(_.id.value).contains(replyId)) {
                      complete(HttpResponse(StatusCodes.NotFound, entity = "There is no such reply."))
                    } else {

                      val paginated = paginatedList(replies, before, after, replyId, cCfg.maxPaginationLimit)
                      complete(paginated)

                    }

                  case Failure(e) => throw e
                }
              case None => complete(HttpResponse(StatusCodes.NotFound, entity = "There is no such post."))
            }
            case Failure(e) => throw e
          }
        }

      }
    }
  }

  def paginatedList(replies: Seq[ForumReply], before: Int, after: Int, replyId: Int, maxPaginationLimit: Int): Seq[ForumReply] = {
    val pivot = replies.map(_.id.value).indexOf(replyId)

    if (maxPaginationLimit < before + after + 1) {
      val proportion = maxPaginationLimit.toFloat / (before + after + 1)
      val newBefore = (before * proportion).round
      val newAfter = (after * proportion).round
      replies.slice(pivot - newBefore, pivot + newAfter)

    } else {

      replies.slice(pivot - before, pivot + after + 1)

    }

  }

}
