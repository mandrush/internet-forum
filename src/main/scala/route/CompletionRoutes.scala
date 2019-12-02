package route

import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import database.schema.{ForumPost, ForumReply}
import domain.logic.ForumJSONSupport

import scala.util.Try

object CompletionRoutes extends ForumJSONSupport {

//  def successfulPost[T](post: ForumPost): Try[T] => Route = {
//    complete(post)
//  }
//
//  def successfulResponse(reply: ForumReply): Route = {
//    complete(reply)
//  }

}
