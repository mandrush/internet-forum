package route

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import domain.forum.entity.Forum.{ForumPost, ForumResponse}
import domain.logic.ForumJSONSupport

object CompletionRoutes extends ForumJSONSupport {

  def successfulPost(post: ForumPost): Route = {
    complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,
      s"""<h1>Post created successfully!</h1>""".stripMargin))
  }

  def successfulResponse(response: ForumResponse): Route = {
    complete(response)
  }

}
