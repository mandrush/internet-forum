import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object Server extends App {

  private implicit val system = ActorSystem("kafeteria")
  private implicit val materializer = ActorMaterializer()
  private implicit val ec = system.dispatcher

  import route.Routes._

  val bindingFuture = Http().bindAndHandle(mainRoute, "localhost", 8080)

}

