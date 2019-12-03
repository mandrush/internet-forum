package route

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, MessageEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import db.DatabaseSetup
import domain.PathNames._
import domain.logic.ForumJSONSupport
import domain.request.UserRequests.{UserCreatePost, UserReply}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import route.MainRoute.ContemporaryConfig
import route.add.NewPostRoute._

import scala.concurrent.Await
import scala.concurrent.duration._

class NewPostSpec extends WordSpec with Matchers with ForumJSONSupport with ScalaFutures with DatabaseSetup with BeforeAndAfter with ScalatestRouteTest {

  before {
    setupDb()
  }

  after {
    dropTables()
  }

  implicit val cCfg = ContemporaryConfig()

  val path = "/" + CreatePost

  val newPostRequest = UserCreatePost(
    topic = "a",
    content = "b",
    nickname = "s",
    email = Some("dziendobry@dowidzenia.pl")
  )

  val someOtherRequest = UserReply(
    nickname = "jasio",
    content = "what??",
    email = Some("dziendobry@dowidzenia.pl")
  )

  private def marshalledNewPost(modified: UserCreatePost): MessageEntity = Marshal(modified).to[MessageEntity].futureValue

  s"Server processing POST requests for $path" should {
    "respond with OK for a proper new post request and save the post in DB" in {

      val post = marshalledNewPost(newPostRequest)
      Post(s"$path").withEntity(post) ~> Route.seal(newPostRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        val selected = Await.result(dbLayer.selectAllPosts(), 2.seconds)
        val saved = selected.head
        saved.topic.value shouldBe newPostRequest.topic
        saved.content.value shouldBe newPostRequest.content
        saved.nickname.value shouldBe newPostRequest.nickname
        saved.email shouldBe newPostRequest.email

      }
    }

    "respond with Bad Request when any other request than the new post request is present" in {
      val wrong = Marshal(someOtherRequest).to[MessageEntity].futureValue
      Post(s"$path").withEntity(wrong) ~> Route.seal(newPostRoute) ~> check {
        response.status shouldBe StatusCodes.BadRequest
      }
    }

//    "respond with Internal Server Error when there's an issue with DB" in {
//      simulateDBMalfunction()
//      val post = marshalledNewPost(newPostRequest)
//      Post(s"$path").withEntity(post) ~> Route.seal(newPostRoute) ~> check {
//        status shouldBe StatusCodes.InternalServerError
//      }
//    }
  }

}
