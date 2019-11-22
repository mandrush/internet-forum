import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class RouteSpec extends WordSpec with Matchers with ScalatestRouteTest {

  import Paths._
  import Routes._

  //https://doc.akka.io/docs/akka-http/current/routing-dsl/testkit.html
  "The service" should {
//    tutaj dajesz slashe przed URI, bo ten Get() ich z automatu nie dodaje najwidoczniej (path() dodaje)

    "return a greeting to /hello path" in {
      Get("/hello") ~> Route.seal(mainRoute) ~> check {
        responseAs[String] shouldBe "<h1>Say hello</h1>"
      }
    }

    "return a JSON for GET requests to /item" in {
      Get("/item") ~> Route.seal(mainRoute) ~> check {
        responseAs[String] shouldBe "{\"decr\":\"description\",\"id\":1}"
      }
    }

//    "leave GET requests to other paths unhandled" in {
//      Get("/asd") ~>
//    }
  }
}
