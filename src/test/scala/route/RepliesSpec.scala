package route

import java.time.Instant

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, MessageEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import database.schema.FieldsValueClasses._
import database.schema.{ForumPost, PK}
import db.DatabaseSetup
import domain.PathNames._
import domain.logic.ForumJSONSupport
import domain.request.UserRequests.{Deletion, UserCreatePost, UserEdit, UserReply}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import route.add.NewReplyRoute._
import route.delete.DeleteReplyRoute._
import route.edit.EditReplyRoute._

import scala.concurrent.Await
import scala.concurrent.duration._

class RepliesSpec extends WordSpec with Matchers with ForumJSONSupport with ScalaFutures with DatabaseSetup with BeforeAndAfterAll with ScalatestRouteTest {

  override protected def beforeAll(): Unit = setupDb()

  var postId = 0L
  var replyId = 0L
  val createReplyPath = "/" + CreateReply
  val editReplyPath = "/" + EditReply
  val deleteReplyPath = "/" + DeleteReply
  implicit val cCfg = Configurator.provideAppConfig()

  val newReplyRequest = UserReply(
    nickname = "jasio",
    content = "what??",
    email = Some("dziendobry@dowidzenia.pl")
  )

  var editReplyRequest = UserEdit(
    newContent = "changed",
    secret = ""
  )

  var deleteReplyRequest = Deletion(secret = "")

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
    Instant.now,
    Instant.now
  )

  s"Server processing POST requests for $createReplyPath" should {
    "respond with OK and create a new reply in DB" in {

      val firstSavePost = dbLayer.insertNewPost(mainPost)
      postId = Await.result(firstSavePost, 2.seconds).value

      val reply = Marshal(newReplyRequest).to[MessageEntity].futureValue
      Post(s"$createReplyPath?post_id=$postId").withEntity(reply) ~> Route.seal(newReplyRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        val selected = Await.result(dbLayer.selectAllReplies(), 2.seconds)
        val saved = selected.head

        saved.parentId shouldBe PK[ForumPost](postId)
        saved.content.value shouldBe newReplyRequest.content
        saved.nickname.value shouldBe newReplyRequest.nickname
        saved.email shouldBe newReplyRequest.email

        replyId = saved.id.value
        editReplyRequest = editReplyRequest.copy(secret = saved.secret.value)
        deleteReplyRequest = deleteReplyRequest.copy(secret = saved.secret.value)
      }

    }

    s"respond with 404 not found when the post_id parameter in path $createReplyPath doesn't match any post in DB" in {
      val reply = Marshal(newReplyRequest).to[MessageEntity].futureValue
      Post(s"$createReplyPath?post_id=12333").withEntity(reply) ~> Route.seal(newReplyRoute) ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    s"respond with 400 Bad Request when a wrong request is issued to $createReplyPath path" in {
      val wrong = Marshal(someOtherRequest).to[MessageEntity].futureValue
      Post(s"$createReplyPath?post_id=1").withEntity(wrong) ~> Route.seal(newReplyRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    s"Respond with 400 Bad Request when the nickname is empty or too long" in {
      val reply = Marshal(newReplyRequest.copy(nickname = "")).to[MessageEntity].futureValue
      Post(s"$createReplyPath?post_id=1").withEntity(reply) ~> Route.seal(newReplyRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val tooLong = "nicknicknicknicknicknicknicknicknicknicknick"
      val wrong = Marshal(newReplyRequest.copy(nickname = tooLong)).to[MessageEntity].futureValue
      Post(s"$createReplyPath?post_id=1").withEntity(wrong) ~> Route.seal(newReplyRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    s"Respond with 400 Bad Request when the content is empty or too long" in {
      val reply = Marshal(newReplyRequest.copy(content = "")).to[MessageEntity].futureValue
      Post(s"$createReplyPath?post_id=1").withEntity(reply) ~> Route.seal(newReplyRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val tooLong =
        """SPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAM
          |SPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAM
          |SPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAM
          |SPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAM
          |SPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAM
        """.stripMargin
      val wrong = Marshal(newReplyRequest.copy(content = tooLong)).to[MessageEntity].futureValue
      Post(s"$createReplyPath?post_id=1").withEntity(wrong) ~> Route.seal(newReplyRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "not create a new reply when the email is malformed and respond with 400 Bad Request" in {
      val badEmailExamples = Seq(
        Some("what@w"),
        Some("what@w."),
        Some("what@w.w.w"),
        Some("what@w.."),
        Some("what.")
      )
      badEmailExamples.map { wrongMail =>
        Post(s"$createReplyPath?post_id=1").withEntity(Marshal(newReplyRequest.copy(email = wrongMail)).to[MessageEntity].futureValue) ~> Route.seal(newReplyRoute) ~> check {
          status shouldBe StatusCodes.BadRequest
        }
      }
    }
  }

  s"Server processing POST requests for $editReplyPath" should {
    "respond with OK for a proper request and properly update the reply with new content using proper secret" in {
      val edit = Marshal(editReplyRequest).to[MessageEntity].futureValue
      Post(s"$editReplyPath?reply_id=$replyId").withEntity(edit) ~> Route.seal(editReplyRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        val selected = Await.result(dbLayer.selectAllReplies(), 2.seconds)
        val edited = selected.head

        edited.content.value shouldBe editReplyRequest.newContent
      }
    }

    s"respond with 404 not found when the reply_id parameter in path $editReplyPath doesn't match any reply in DB" in {
      val edited = Marshal(editReplyRequest).to[MessageEntity].futureValue
      Post(s"$editReplyPath?reply_id=12333").withEntity(edited) ~> Route.seal(editReplyRoute) ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    s"respond with 400 Bad Request when a wrong request is issued to $editReplyPath path" in {
      val wrong = Marshal(someOtherRequest).to[MessageEntity].futureValue
      Post(s"$editReplyPath?reply_id=$replyId").withEntity(wrong) ~> Route.seal(editReplyRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "respond with 401 Unauthorized when the request has a wrong secret" in {
      val badSecretReq = UserEdit(
        newContent = "asdsasd",
        secret = "11"
      )
      val incorrect = Marshal(badSecretReq).to[MessageEntity].futureValue
      Post(s"$editReplyPath?reply_id=$replyId").withEntity(incorrect) ~> Route.seal(editReplyRoute) ~> check {
        status shouldBe StatusCodes.Unauthorized
      }
    }

    "respond with 400 Bad Request and not edit the reply when the new content is empty or too long" in {
      val reply = Marshal(editReplyRequest.copy(newContent = "")).to[MessageEntity].futureValue
      Post(s"$editReplyPath?reply_id=$replyId").withEntity(reply) ~> Route.seal(editReplyRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val tooLong =
        """SPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAM
          |SPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAM
          |SPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAM
          |SPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAM
          |SPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAM
        """.stripMargin
      val tooLongReply = Marshal(editReplyRequest.copy(newContent = tooLong)).to[MessageEntity].futureValue
      Post(s"$editReplyPath?reply_id=$replyId").withEntity(tooLongReply) ~> Route.seal(editReplyRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

  }

  s"Server processing POST requests for $deleteReplyPath" should {

    "respond with 401 Unauthorized when the deletion request has a wrong secret" in {
      val badSecretReq = Deletion(
        secret = "11"
      )
      val incorrect = Marshal(badSecretReq).to[MessageEntity].futureValue
      Post(s"$deleteReplyPath?reply_id=$replyId").withEntity(incorrect) ~> Route.seal(deleteReplyRoute) ~> check {
        status shouldBe StatusCodes.Unauthorized
      }
    }

    "delete a given reply from DB" in {
      val delete = Marshal(deleteReplyRequest).to[MessageEntity].futureValue
      Post(s"$deleteReplyPath?reply_id=$replyId").withEntity(delete) ~> Route.seal(deleteReplyRoute) ~> check {
        status shouldBe StatusCodes.OK

        val goneReply = dbLayer.findReply(replyId)
        val gone = Await.result(goneReply, 2.seconds)

        gone.isEmpty shouldBe true
      }
    }
  }
}
