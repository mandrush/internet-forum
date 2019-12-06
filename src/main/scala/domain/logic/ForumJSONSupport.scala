package domain.logic
import java.time.Instant

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import database.schema.FieldsValueClasses.{Content, Nickname, Secret, Topic}
import database.schema.{ForumPost, ForumReply, PK}
import domain.request.UserRequests.{Deletion, UserCreatePost, UserEdit, UserReply}
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
      "created_at" -> JsString(obj.createTs.toString),
      "last_update" -> JsString(obj.updateTs.toString),
      "id" -> JsString(obj.id.value.toString)
    )

    override def read(json: JsValue): ForumPost = {
      json.asJsObject.getFields("content", "created_at", "email", "id", "last_update", "nickname", "secret", "topic") match {
        case Seq(JsString(content), JsString(createdAt), JsString(email), JsString(id), JsString(lastUpdate), JsString(nickname), JsString(secret), JsString(topic)) =>
          ForumPost(Topic(topic), Content(content), Nickname(nickname), Some(email), Secret(secret), Instant.parse(createdAt), Instant.parse(lastUpdate), PK[ForumPost](id.toLong))
        case _ => throw DeserializationException("ForumPost expected")
      }
    }
  }

  implicit object ForumReplyFormat extends RootJsonFormat[ForumReply] {
    override def write(obj: ForumReply): JsValue = JsObject(
      "nickname" -> JsString(obj.nickname.value),
      "content" -> JsString(obj.content.value),
      "email" -> obj.email.toJson,
      "secret" -> JsString(obj.secret.value),
      "timestamp" -> JsString(obj.timestamp.toString),
      "id" -> JsString(obj.id.value.toString),
      "parent_id" -> JsString(obj.parentId.value.toString)
    )

    override def read(json: JsValue): ForumReply = {
      json.asJsObject.getFields("content", "email", "id", "nickname", "secret", "timestamp", "parent_id") match {
        case Seq(JsString(content), JsString(email), JsString(id), JsString(nickname), JsString(secret), JsString(timestamp), JsString(parentId)) =>
          ForumReply(Content(content), Nickname(nickname), Some(email), Instant.parse(timestamp), Secret(secret), PK[ForumPost](parentId.toLong), PK[ForumReply](id.toLong))
        case _ => throw DeserializationException("ForumReply expected")
      }
    }
  }

  implicit val userCreateFormat = jsonFormat4(UserCreatePost)

  implicit val userReplyFormat = jsonFormat3(UserReply)

  implicit val userEditFormat = jsonFormat2(UserEdit)

  implicit val deleteFormat = jsonFormat1(Deletion)

}
