package domain.logic
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import domain.Domain.{ForumPost, ForumResponse, BasicForumEntity}
import spray.json.DefaultJsonProtocol
//https://doc.akka.io/docs/akka-http/current/common/json-support.html
trait ForumJSONSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val postFormat = jsonFormat5(ForumPost)
  implicit val forumResponseFormat = jsonFormat4(ForumResponse)
  implicit val userResponseFormat = jsonFormat3(BasicForumEntity)
}
