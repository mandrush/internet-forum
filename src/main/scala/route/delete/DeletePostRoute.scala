package route.delete

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{MalformedFormFieldRejection, Route}
import database.layer.DatabaseLayer
import domain.PathNames.DeletePost
import domain.logic.{FieldsValidation, ForumJSONSupport}
import domain.request.UserRequests.Deletion
import route.AppConfig
import spray.json.JsValue

import scala.util.{Failure, Success}

object DeletePostRoute extends ForumJSONSupport with FieldsValidation {

  def deletePostRoute(implicit dbLayer: DatabaseLayer,
                      cCfg: AppConfig): Route = {
    path(DeletePost) {
      parameter('post_id.as[Long]) { postId =>
        entity(as[JsValue]) { req =>
          if (!onlyContains(req, "secret")) {
            reject(MalformedFormFieldRejection("", s"Only secret field is allowed here"))
          } else {
            entity(as[Deletion]) { delete =>
              val maybePost = dbLayer.findPost(postId)
              onComplete(maybePost) {
                case Success(post) => post match {
                  case Some(p) =>
                    if (delete.secret == p.secret.value) {
                      val deleteF = dbLayer.deletePost(postId)
                      onComplete(deleteF) {
                        case Success(_) => complete(HttpResponse(StatusCodes.OK, entity = s"Deleted post with id = $postId"))
                        case Failure(e) => throw e
                      }
                    } else {
                      complete(HttpResponse(StatusCodes.Unauthorized, entity = "Wrong secret"))
                    }
                  case None    => complete(HttpResponse(StatusCodes.NotFound, entity = "There is no such post."))
                }
                case Failure(e) => throw e
              }
            }
          }
        }
      }
    }
  }

}
