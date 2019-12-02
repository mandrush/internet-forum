package domain.logic
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import database.schema.{ForumPost, ForumReply}
import domain.request.UserRequests.{UserCreatePost, UserReply}
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonFormat}
import spray.json._
//https://doc.akka.io/docs/akka-http/current/common/json-support.html
trait ForumJSONSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object ForumPostFormat extends RootJsonFormat[ForumPost] {
    override def write(obj: ForumPost): JsValue = JsObject(
      "topic" -> JsString(obj.topic.value),
      "nickname" -> JsString(obj.nickname.value),
      "content" -> JsString(obj.content.value),
      "email" -> obj.email.toJson,
      "secret" -> JsString(obj.secret.value),
      "timestamp" -> JsString(obj.timestamp.toString)
    )

    override def read(json: JsValue): ForumPost = ???
  }

  implicit object ForumReplyFormat extends RootJsonFormat[ForumReply] {
    override def write(obj: ForumReply): JsValue = JsObject(
      "nickname" -> JsString(obj.nickname.value),
      "content" -> JsString(obj.content.value),
      "email" -> obj.email.toJson,
      "secret" -> JsString(obj.secret.value),
      "timestamp" -> JsString(obj.timestamp.toString)
    )

    override def read(json: JsValue): ForumReply = ???
  }

  implicit val userCreateFormat = jsonFormat4(UserCreatePost)

  implicit val userReplyFormat = jsonFormat3(UserReply)

}
