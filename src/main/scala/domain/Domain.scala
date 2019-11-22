package domain

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import domain.Domain.{Item, ForumPost}
import spray.json.DefaultJsonProtocol
object Domain {

  final case class Item(id: Int, decr: String)
//  jsonFormatX gdzie X - ile jest pol w case klasie
//  https://doc.akka.io/docs/akka-http/current/common/json-support.html

//  TODO add parent to ForumPost
  final case class ForumPost(topic: Option[String], content: Option[String], nickname: Option[String], email: Option[String])
}

trait JSONSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val itemFormat = jsonFormat2(Item)
  implicit val postFormat = jsonFormat4(ForumPost)
}
