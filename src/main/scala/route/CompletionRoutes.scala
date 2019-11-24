package route

import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import domain.forum.entity.Forum.{ForumPost, ForumResponse}
import domain.logic.ForumJSONSupport

object CompletionRoutes extends ForumJSONSupport {

  def successfulPost(post: ForumPost): Route = {
    complete(post)
  }

  def successfulResponse(response: ForumResponse): Route = {
    complete(response)
  }

}
