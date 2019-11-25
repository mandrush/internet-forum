package domain.logic
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import domain.forum.Forum.{ForumPost, ForumResponse}
import domain.request.UserRequests.{UserCreatePost, UserEditPost, UserReply}
import spray.json.DefaultJsonProtocol
//https://doc.akka.io/docs/akka-http/current/common/json-support.html
trait ForumJSONSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val postFormat = jsonFormat5(ForumPost)

  implicit val forumResponseFormat = jsonFormat5(ForumResponse)

  implicit val userCreateFormat = jsonFormat4(UserCreatePost)

  implicit val userReplyFormat = jsonFormat3(UserReply)

  implicit val userEditFormat = jsonFormat2(UserEditPost)

}
