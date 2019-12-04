package route

import java.time.Instant

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, MessageEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import database.schema.FieldsValueClasses.{Content, Nickname, Secret}
import database.schema.{ForumPost, ForumReply, PK}
import db.DatabaseSetup
import domain.PathNames._
import domain.logic.ForumJSONSupport
import domain.request.UserRequests
import domain.request.UserRequests.{Deletion, UserCreatePost, UserEdit, UserReply}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import route.MainRoute.ContemporaryConfig
import route.add.NewPostRoute._
import route.edit.EditPostRoute._
import route.delete.DeletePostRoute._

import scala.concurrent.Await
import scala.concurrent.duration._

class PostsSpec extends WordSpec with Matchers with ForumJSONSupport with ScalaFutures with DatabaseSetup with BeforeAndAfterAll with ScalatestRouteTest {

  override protected def beforeAll(): Unit = {
    setupDb()
  }

  implicit val cCfg = ContemporaryConfig()

  val createPostPath = "/" + CreatePost
  val editPostPath = "/" + EditPost
  val deletePostPath = "/" + DeletePost

  var postId = 0L

  val newPostRequest = UserCreatePost(
    topic = "a",
    content = "b",
    nickname = "s",
    email = Some("dziendobry@dowidzenia.pl")
  )

  var editPostRequest = UserEdit(
    newContent = "siemanko",
    secret = ""
  )

  var deletePostRequest = Deletion(
    secret = ""
  )

  val someOtherRequest = UserReply(
    nickname = "jasio",
    content = "what??",
    email = Some("dziendobry@dowidzenia.pl")
  )

  private def marshalledNewPost(modified: UserCreatePost): MessageEntity = Marshal(modified).to[MessageEntity].futureValue

  s"Server processing POST requests for $createPostPath" should {
    "respond with OK for a proper new post request and save the post in DB" in {

      val post = Marshal(newPostRequest).to[MessageEntity].futureValue
      Post(s"$createPostPath").withEntity(post) ~> Route.seal(newPostRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        val selected = Await.result(dbLayer.selectAllPosts(), 2.seconds)
        val saved = selected.head
        saved.topic.value shouldBe newPostRequest.topic
        saved.content.value shouldBe newPostRequest.content
        saved.nickname.value shouldBe newPostRequest.nickname
        saved.email shouldBe newPostRequest.email

        postId = saved.id.value
        editPostRequest = editPostRequest.copy(secret = saved.secret.value)
        deletePostRequest = deletePostRequest.copy(secret = saved.secret.value)
      }
    }

    "respond with Bad Request when any other request than the new post request is present" in {
      val wrong = Marshal(someOtherRequest).to[MessageEntity].futureValue
      Post(s"$createPostPath").withEntity(wrong) ~> Route.seal(newPostRoute) ~> check {
        response.status shouldBe StatusCodes.BadRequest
      }
    }
  }

  s"Server processing POST requests for $editPostPath" should {
    "respond with OK for a proper post edit request and properly update the post in DB" in {
      val edit = Marshal(editPostRequest).to[MessageEntity].futureValue
      Post(s"$editPostPath?post_id=$postId").withEntity(edit) ~> Route.seal(editPostRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        val selected = Await.result(dbLayer.selectAllPosts(), 2.seconds)
        val edited = selected.head

        edited.content.value shouldBe editPostRequest.newContent
      }
    }

    s"respond with 404 Not Found when the post_id parameter in path $editPostPath doesn't match any existing posts" in {
      val edit = Marshal(editPostRequest).to[MessageEntity].futureValue
      Post(s"$editPostPath?post_id=111111").withEntity(edit) ~> Route.seal(editPostRoute) ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "respond with 400 Bad Request when the edit post request is incorrect" in {
      val wrong = Marshal(someOtherRequest).to[MessageEntity].futureValue
      Post(s"$editPostPath?post_id=$postId").withEntity(wrong) ~> Route.seal(editPostRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "respond with 401 Unauthorized when the request has a wrong secret" in {
      val badSecretReq = UserEdit(
        newContent = "SSSSS",
        secret = "ciuciubabka"
      )
      val incorrect = Marshal(badSecretReq).to[MessageEntity].futureValue
      Post(s"$editPostPath?post_id=$postId").withEntity(incorrect) ~> Route.seal(editPostRoute) ~> check {
        status shouldBe StatusCodes.Unauthorized
      }
    }
  }

  s"Server processing POST requests for $deletePostPath" should {
    "respond with 401 Unauthorized when the deletion request has a wrong secret" in {
      val badSecretReq = UserRequests.Deletion(
        secret = "ciuciubabka"
      )
      val incorrect = Marshal(badSecretReq).to[MessageEntity].futureValue
      Post(s"$deletePostPath?post_id=$postId").withEntity(incorrect) ~> Route.seal(deletePostRoute) ~> check {
        status shouldBe StatusCodes.Unauthorized
      }
    }

    "respond with 200 OK and delete a given post along with all of its replies from DB" in {
      val replyA = ForumReply(
        Content("sd"),
        Nickname("sdSD"),
        Some("dlab@wp.pl"),
        Instant.now,
        Secret("123"),
        parentId = PK[ForumPost](postId)
      )
      val replyB = ForumReply(
        Content("1111sd"),
        Nickname("s2323dSD"),
        Some("dlab@wp.pl"),
        Instant.now,
        Secret("123"),
        parentId = PK[ForumPost](postId)
      )

      Await.result(dbLayer.insertNewReply(replyA), 2.seconds)
      Await.result(dbLayer.insertNewReply(replyB), 2.seconds)

      val delete = Marshal(deletePostRequest).to[MessageEntity].futureValue
      Post(s"$deletePostPath?post_id=$postId").withEntity(delete) ~> Route.seal(deletePostRoute) ~> check {
        status shouldBe StatusCodes.OK

        val gonePost = dbLayer.findPost(postId)
        val gone = Await.result(gonePost, 2.seconds)

        gone.isEmpty shouldBe true

        val replies = Await.result(dbLayer.selectAllReplies(), 2.seconds)
        replies shouldBe empty
      }
    }
  }

}

//    "respond with Internal Server Error when there's an issue with DB" in {
//      simulateDBMalfunction()
//      val post = marshalledNewPost(newPostRequest)
//      Post(s"$path").withEntity(post) ~> Route.seal(newPostRoute) ~> check {
//        status shouldBe StatusCodes.InternalServerError
//      }
//    }