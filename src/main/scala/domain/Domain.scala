package domain

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import domain.Domain.ForumPost
import spray.json.DefaultJsonProtocol
object Domain {

//  https://doc.akka.io/docs/akka-http/current/common/json-support.html

//  TODO add parent to ForumPost
  final case class ForumPost(topic: Option[String], content: Option[String], nickname: Option[String], email: Option[String])
}

trait JSONSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val postFormat = jsonFormat4(ForumPost)
}
