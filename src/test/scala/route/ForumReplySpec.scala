package route
/*
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, MessageEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import domain.forum.Forum
import domain.logic.ForumJSONSupport
import domain.request.UserRequests.{UserCreatePost, UserReply}
import org.scalatest.{Matchers, WordSpec}
import route.Routes.mainRoute
import org.scalatest.concurrent.ScalaFutures._

class ForumReplySpec extends WordSpec with Matchers with ScalatestRouteTest with ForumJSONSupport {

  import Forum._
  import domain.InMemoryDB._
  import domain.PathNames._

  private def marshalledNewReply(modified: UserReply): MessageEntity = Marshal(modified).to[MessageEntity].futureValue

  val path = "/" + CreateReply

  val mainPost = ForumPost(
    id       = 1,
    topic    = Some("Hi!"),
    nickname = Some("thesaurus"),
    content  = Some("How do I learn coding?"),
    email    = Some("dziendobry@dowidzenia.pl")
  )

  val userCreatesATopic = UserCreatePost(
    topic = Some("wrong"),
    content = Some("a"),
    nickname = Some("ASD"),
    email = Some("jasio@wp.pl")
  )

  val userReply = UserReply(
    nickname = Some("jasio"),
    email    = Some("jasio@wp.pl"),
    content  = Some("work hard bro")
  )

  posts += mainPost
  private val properId = mainPost.id

  s"POST request for $path" should {
    "create a forum response after a user submits a request with nickname, content and email" in {
      Post(s"$path/$properId").withEntity(marshalledNewReply(userReply)) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
      }
    }

    "NOT create a forum response after a wrong request is sent" in {
      val wrong = Marshal(userCreatesATopic).to[MessageEntity].futureValue
      Post(s"$path/$properId").withEntity(wrong) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "respond with 404 Not Found if there's no such topic with such id" in {
      val invalidId = "1232"
      Post(s"$path/$invalidId").withEntity(marshalledNewReply(userReply)) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "be rejected when user enters a malformed e-mail" in {
      val badDomain = Some("what@w")
      Post(s"$path/$properId").withEntity(marshalledNewReply(userReply.copy(email = badDomain))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val anotherBadDomain = Some("what@w.")
      Post(s"$path/$properId").withEntity(marshalledNewReply(userReply.copy(email = anotherBadDomain))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val tooManyDots = Some("what@w.w.w")
      Post(s"$path/$properId").withEntity(marshalledNewReply(userReply.copy(email = tooManyDots))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val alsoTooManyDots = Some("what@w..")
      Post(s"$path/$properId").withEntity(marshalledNewReply(userReply.copy(email = alsoTooManyDots))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val singleWord = Some("what.")
      Post(s"$path/$properId").withEntity(marshalledNewReply(userReply.copy(email = singleWord))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "be rejected when user's nickname is too long" in {
      val tooLong = Some("nicknicknicknicknicknicknicknicknicknicknick")
      Post(s"$path/$properId").withEntity(marshalledNewReply(userReply.copy(nickname = tooLong))) ~> Route.seal(mainRoute) ~> check {
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
      Post(s"$path/$properId").withEntity(marshalledNewReply(userReply.copy(content = tooLong))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "be rejected when any of the fields is not filled by the user (empty string)" in {
      val empty = Some("")
      Post(s"$path/$properId").withEntity(marshalledNewReply(userReply.copy(nickname = empty))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path/$properId").withEntity(marshalledNewReply(userReply.copy(content = empty))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path/$properId").withEntity(marshalledNewReply(userReply.copy(email = empty))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "be rejected if any of the fields is for some reason None" in {
      Post(s"$path/$properId").withEntity(marshalledNewReply(userReply.copy(nickname = None))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path/$properId").withEntity(marshalledNewReply(userReply.copy(content = None))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path/$properId").withEntity(marshalledNewReply(userReply.copy(email = None))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }
  }


}
*/