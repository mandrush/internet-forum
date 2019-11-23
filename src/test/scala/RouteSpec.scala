import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class RouteSpec extends WordSpec with Matchers with ScalatestRouteTest {

  import domain.PathNames._
  import route.Routes._

  //https://doc.akka.io/docs/akka-http/current/routing-dsl/testkit.html
  "The service" should {
    "return a greeting to /hello path" in {
      Get(s"/$HelloPath") ~> Route.seal(mainRoute) ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldBe "<h1>Say hello</h1>"
      }
    }

//    "leave GET requests to other paths unhandled" in {
//      Get("/asd") ~>
//    }
  }
}
