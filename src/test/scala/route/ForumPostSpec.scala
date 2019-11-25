package route

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, MessageEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import domain.forum.Forum.ForumPost
import domain.logic.ForumJSONSupport
import domain.request.UserRequests.{UserCostam, UserCreatePost, UserReply}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class ForumPostSpec extends WordSpec with Matchers with ScalatestRouteTest with ForumJSONSupport with ScalaFutures {

  import domain.PathNames._
  import route.Routes._

  val path = "/" + CreatePost
  val userPostRequest = UserCostam(
    topic    = Some("Hi!"),
    nickname = Some("thesaurus"),
    content  = Some("How do I learn coding?"),
    email    = Some("dziendobry@dowidzenia.pl")
  )

  private def marshalledNewPost(modified: UserCostam): MessageEntity = Marshal(modified).to[MessageEntity].futureValue

  s"POST request for $path" should {
    "create a forum post" in {
      val post = Marshal(userPostRequest).to[MessageEntity].futureValue
      Post(s"$path").withEntity(post) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        val newpost = Unmarshal(response).to[ForumPost].futureValue
        newpost.nickname shouldBe userPostRequest.nickname
        newpost.topic shouldBe userPostRequest.topic
        newpost.content shouldBe userPostRequest.content
        newpost.email shouldBe userPostRequest.email
      }
    }

    "NOT create a forum post when the topic was empty" in {
      val post = Marshal(userPostRequest.copy(topic = None)).to[MessageEntity].futureValue
      Post(s"$path").withEntity(post) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "be rejected when user enters a malformed e-mail" in {
      val badDomain = Some("what@w")
      Post(s"$path").withEntity(marshalledNewPost(userPostRequest.copy(email = badDomain))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val anotherBadDomain = Some("what@w.")
      Post(s"$path").withEntity(marshalledNewPost(userPostRequest.copy(email = anotherBadDomain))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val tooManyDots = Some("what@w.w.w")
      Post(s"$path").withEntity(marshalledNewPost(userPostRequest.copy(email = tooManyDots))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val alsoTooManyDots = Some("what@w..")
      Post(s"$path").withEntity(marshalledNewPost(userPostRequest.copy(email = alsoTooManyDots))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val singleWord = Some("what.")
      Post(s"$path").withEntity(marshalledNewPost(userPostRequest.copy(email = singleWord))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "be rejected when user's nickname is too long" in {
      val tooLong = Some("nicknicknicknicknicknicknicknicknicknicknick")
      Post(s"$path").withEntity(marshalledNewPost(userPostRequest.copy(nickname = tooLong))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "be rejected when post's topic is too long" in {
      val tooLong = Some("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
      Post(s"$path").withEntity(marshalledNewPost(userPostRequest.copy(topic = tooLong))) ~> Route.seal(mainRoute) ~> check {
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
      Post(s"$path").withEntity(marshalledNewPost(userPostRequest.copy(content = tooLong))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "be rejected when any of the fields is not filled by the user (empty string)" in {
      val empty = Some("")
      Post(s"$path").withEntity(marshalledNewPost(userPostRequest.copy(nickname = empty))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path").withEntity(marshalledNewPost(userPostRequest.copy(topic = empty))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path").withEntity(marshalledNewPost(userPostRequest.copy(content = empty))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path").withEntity(marshalledNewPost(userPostRequest.copy(email = empty))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "be rejected if any of the fields is for some reason None" in {
      Post(s"$path").withEntity(marshalledNewPost(userPostRequest.copy(nickname = None))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path").withEntity(marshalledNewPost(userPostRequest.copy(topic = None))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path").withEntity(marshalledNewPost(userPostRequest.copy(content = None))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path").withEntity(marshalledNewPost(userPostRequest.copy(email = None))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

  }

}
