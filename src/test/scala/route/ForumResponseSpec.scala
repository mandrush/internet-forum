package route

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, MessageEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import domain.forum.entity.Forum
import domain.logic.ForumJSONSupport
import org.scalatest.{Matchers, WordSpec}
import route.Routes.mainRoute
import org.scalatest.concurrent.ScalaFutures._

class ForumResponseSpec extends WordSpec with Matchers with ScalatestRouteTest with ForumJSONSupport {

  import Forum._
  import domain.InMemoryDB._
  import domain.PathNames._

  private def marshalledNewPost(modified: BasicForumEntity): MessageEntity = Marshal(modified).to[MessageEntity].futureValue

  val path = "/" + CreateResponse

  val mainPost = ForumPost(
    id       = 1,
    topic    = Some("Hi!"),
    nickname = Some("thesaurus"),
    content  = Some("How do I learn coding?"),
    email    = Some("dziendobry@dowidzenia.pl")
  )

  val userIssuedPost = BasicForumEntity(
    topic    = None,
    nickname = Some("jasio"),
    content  = Some("work hard bro"),
    email    = Some("jasio@wp.pl")
  )

  posts += mainPost
  private val marshalled = Marshal(userIssuedPost).to[MessageEntity].futureValue
  private val properId = mainPost.id

  "Forum response" should {
    "be created after a user submits a POST request with nickname, content and email" in {

      Post(s"$path/$properId").withEntity(marshalled) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
      }
    }

    "respond with 404 Not Found if there's no such topic with such id" in {
      val invalidId = "123"
      Post(s"$path/$invalidId").withEntity(marshalled) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "be rejected when user enters a malformed e-mail" in {
      val badDomain = Some("what@w")
      Post(s"$path/$properId").withEntity(marshalledNewPost(userIssuedPost.copy(email = badDomain))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val anotherBadDomain = Some("what@w.")
      Post(s"$path/$properId").withEntity(marshalledNewPost(userIssuedPost.copy(email = anotherBadDomain))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val tooManyDots = Some("what@w.w.w")
      Post(s"$path/$properId").withEntity(marshalledNewPost(userIssuedPost.copy(email = tooManyDots))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val alsoTooManyDots = Some("what@w..")
      Post(s"$path/$properId").withEntity(marshalledNewPost(userIssuedPost.copy(email = alsoTooManyDots))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val singleWord = Some("what.")
      Post(s"$path/$properId").withEntity(marshalledNewPost(userIssuedPost.copy(email = singleWord))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "be rejected when user's nickname is too long" in {
      val tooLong = Some("nicknicknicknicknicknicknicknicknicknicknick")
      Post(s"$path/$properId").withEntity(marshalledNewPost(userIssuedPost.copy(nickname = tooLong))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "be rejected when post's content is too long" in {
      // max is 400 characters
      val tooLong = Some(
        """SPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAM
          |SPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAM
          |SPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAM
          |SPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAM
          |SPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAMSPAM
        """.stripMargin)
      Post(s"$path/$properId").withEntity(marshalledNewPost(userIssuedPost.copy(content = tooLong))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "be rejected when any of the fields is not filled by the user (empty string)" in {
      val empty = Some("")
      Post(s"$path/$properId").withEntity(marshalledNewPost(userIssuedPost.copy(nickname = empty))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path/$properId").withEntity(marshalledNewPost(userIssuedPost.copy(content = empty))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path/$properId").withEntity(marshalledNewPost(userIssuedPost.copy(email = empty))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "be rejected if any of the fields is for some reason None" in {
      Post(s"$path/$properId").withEntity(marshalledNewPost(userIssuedPost.copy(nickname = None))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path/$properId").withEntity(marshalledNewPost(userIssuedPost.copy(content = None))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path/$properId").withEntity(marshalledNewPost(userIssuedPost.copy(email = None))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }
  }


}
