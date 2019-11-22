import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, MessageEntity, StatusCodes}
import akka.http.scaladsl.server.ContentNegotiator.Alternative.ContentType
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import domain.Domain.ForumPost
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.{Matchers, WordSpec}

class RouteSpec extends WordSpec with Matchers with ScalatestRouteTest {

  import route.Routes._
  import domain.Paths._

  //https://doc.akka.io/docs/akka-http/current/routing-dsl/testkit.html
  "The service" should {
//    tutaj dajesz slashe przed URI, bo ten Get() ich z automatu nie dodaje najwidoczniej (path() dodaje)

    "return a greeting to /hello path" in {
      Get(s"/$HelloPath") ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldBe "<h1>Say hello</h1>"
      }
    }

    "return a JSON for GET requests to /item" in {
      Get(s"/$ItemPath") ~> Route.seal(mainRoute) ~> check {
        responseAs[String] shouldBe "{\"decr\":\"description\",\"id\":1}"
      }
    }

    "create a Post for POST request to /create-post" in {
      val topic = Some("hi")
      val nickname = Some("scoobydoo")
      val content = Some("woof")
      val email = Some("scooby@doo.doo")
      val post = Marshal(ForumPost(topic, content, nickname, email)).to[MessageEntity].futureValue
      Post(s"/$CreatePost").withEntity(post) ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe responseAs[String]
      }
    }

//    "leave GET requests to other paths unhandled" in {
//      Get("/asd") ~>
//    }
  }
}
