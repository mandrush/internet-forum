import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.io.StdIn
import scala.util.{Failure, Success}

object Client extends App {
//https://doc.akka.io/docs/akka-http/current/client-side/request-level.html
//https://medium.com/@pwdd/testing-akka-http-clients-59fa2d0545c1

  private implicit val system = ActorSystem()
  private implicit val materializer = ActorMaterializer()
  private implicit val ec = system.dispatcher

  def processRequest(req: HttpRequest): Unit = {
  Http().singleRequest(req).onComplete {
    case Success(resp) => println(resp)
    case Failure(_) => sys.error("Something wrong")
  }
}

  @tailrec
  def loop: Unit = {
    println("1 for a greeting; 2 for an item")
    StdIn.readInt() match {
      case 1 => processRequest(helloReq)     ;  loop
      case 2 => processRequest(itemReq)      ;  loop
      case _ => println("Wrong! Try again: ");  loop
    }
  }

  val helloReq = HttpRequest(uri = "http://localhost:8080/hello")
  val itemReq = HttpRequest(uri = "http://localhost:8080/item")

  loop

}
