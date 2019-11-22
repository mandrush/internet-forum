
import akka.actor.ActorSystem
import akka.http.javadsl.server.Route
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directive0, StandardRoute}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext


object Main extends App {
  //serwer http, zeby odpytywac mozna napisac w akka http klienta http
  private implicit val system = ActorSystem("asd")
  private implicit val materializer = ActorMaterializer()
  private implicit val ec = system.dispatcher

  import Routes._

  val bindingFuture = Http().bindAndHandle(mainRoute, "localhost", 8080)

}

