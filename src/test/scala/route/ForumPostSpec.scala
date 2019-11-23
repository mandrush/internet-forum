package route

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, MessageEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import domain.Domain.ForumPost
import domain.logic.ForumJSONSupport
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.{Matchers, WordSpec}

class ForumPostSpec extends WordSpec with Matchers with ScalatestRouteTest with ForumJSONSupport {

  import route.Routes._
  import domain.PathNames._

  val path = "/" + CreatePost
  val forumPost = ForumPost(
    id       = 1,
    topic    = Some("Hi!"),
    nickname = Some("thesaurus"),
    content  = Some("How do I learn coding?"),
    email    = Some("dziendobry@dowidzenia.pl")
  )

  private def marshalledNewPost(modified: ForumPost): MessageEntity = Marshal(modified).to[MessageEntity].futureValue

  s"POST request for $path" should {
    "create a forum post" in {
      val post = Marshal(forumPost).to[MessageEntity].futureValue
      Post(s"$path").withEntity(post) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`text/html(UTF-8)`
        entityAs[String] shouldBe responseAs[String]
      }
    }

    "be rejected when user enters a malformed e-mail" in {
      val badDomain = Some("what@w")
      Post(s"$path").withEntity(marshalledNewPost(forumPost.copy(email = badDomain))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val anotherBadDomain = Some("what@w.")
      Post(s"$path").withEntity(marshalledNewPost(forumPost.copy(email = anotherBadDomain))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val tooManyDots = Some("what@w.w.w")
      Post(s"$path").withEntity(marshalledNewPost(forumPost.copy(email = tooManyDots))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val alsoTooManyDots = Some("what@w..")
      Post(s"$path").withEntity(marshalledNewPost(forumPost.copy(email = alsoTooManyDots))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }

      val singleWord = Some("what.")
      Post(s"$path").withEntity(marshalledNewPost(forumPost.copy(email = singleWord))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "be rejected when user's nickname is too long" in {
      val tooLong = Some("nicknicknicknicknicknicknicknicknicknicknick")
      Post(s"$path").withEntity(marshalledNewPost(forumPost.copy(nickname = tooLong))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "be rejected when post's topic is too long" in {
      val tooLong = Some("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
      Post(s"$path").withEntity(marshalledNewPost(forumPost.copy(topic = tooLong))) ~> Route.seal(mainRoute) ~> check {
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
      Post(s"$path").withEntity(marshalledNewPost(forumPost.copy(content = tooLong))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "be rejected when any of the fields is not filled by the user (empty string)" in {
      val empty = Some("")
      Post(s"$path").withEntity(marshalledNewPost(forumPost.copy(nickname = empty))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path").withEntity(marshalledNewPost(forumPost.copy(topic = empty))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path").withEntity(marshalledNewPost(forumPost.copy(content = empty))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path").withEntity(marshalledNewPost(forumPost.copy(email = empty))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "be rejected if any of the fields is for some reason None" in {
      Post(s"$path").withEntity(marshalledNewPost(forumPost.copy(nickname = None))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path").withEntity(marshalledNewPost(forumPost.copy(topic = None))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path").withEntity(marshalledNewPost(forumPost.copy(content = None))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      Post(s"$path").withEntity(marshalledNewPost(forumPost.copy(email = None))) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

  }

}
