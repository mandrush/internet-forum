package route

import java.time.Instant

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, MessageEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import database.schema.{ForumPost, PK}
import db.DatabaseSetup
import domain.PathNames._
import domain.logic.ForumJSONSupport
import domain.request.UserRequests.{UserCreatePost, UserReply}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import route.MainRoute.ContemporaryConfig
import database.schema.FieldsValueClasses._

import scala.concurrent.duration._
import scala.concurrent.Await
import route.add.NewReplyRoute._

class NewReplySpec extends WordSpec with Matchers with ForumJSONSupport with ScalaFutures with DatabaseSetup with BeforeAndAfter with ScalatestRouteTest {

  before {
    setupDb()


  }

  val postId = 0L
  val path = "/" + CreateReply
  implicit val cCfg = ContemporaryConfig()

  val newReplyRequest = UserReply(
    nickname = "jasio",
    content = "what??",
    email = Some("dziendobry@dowidzenia.pl")
  )

  val someOtherRequest = UserCreatePost(
    topic = "a",
    content = "b",
    nickname = "s",
    email = Some("dziendobry@dowidzenia.pl")
  )

  val mainPost = ForumPost(
    Topic("dddd"),
    Content("ASD"),
    Nickname("@@@@"),
    Some("halo@jasio.pl"),
    Secret("123"),
    Instant.now
  )

  private def marshalledNewReply(modified: UserReply): MessageEntity = Marshal(modified).to[MessageEntity].futureValue

  s"Server processing POST requests for $path" should {
    "respond with OK and create a new reply in DB" in {

      val firstSavePost = dbLayer.insertNewPost(mainPost)
      val postId = Await.result(firstSavePost, 2.seconds)

      val reply = marshalledNewReply(newReplyRequest)
      Post(s"$path?post_id=${postId.value}").withEntity(reply) ~> Route.seal(newReplyRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        val selected = Await.result(dbLayer.selectAllReplies(), 2.seconds)
        val saved = selected.head

        saved.parentId shouldBe PK[ForumPost](postId.value)
        saved.content.value shouldBe newReplyRequest.content
        saved.nickname.value shouldBe newReplyRequest.nickname
        saved.email shouldBe newReplyRequest.email
      }

    }

    "respond with 404 not found when the post_id parameter in URL doesn't match any post in DB" in {
      val reply = marshalledNewReply(newReplyRequest)
      Post(s"$path?post_id=12333").withEntity(reply) ~> Route.seal(newReplyRoute) ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    s"respond with 400 Bad Request when a wrong request is issued to $path path" in {
      val wrong = Marshal(someOtherRequest).to[MessageEntity].futureValue
      Post(s"$path?post_id=1").withEntity(wrong) ~> Route.seal(newReplyRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }


  }

}
