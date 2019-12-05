import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import database.layer.DatabaseLayer
import logger.StandaloneLogger
import route.{Configurator, MainRoute}
import slick.jdbc.PostgresProfile

object Server extends App with StandaloneLogger {

  implicit val system = ActorSystem("kafeteria")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  implicit val appCfg = Configurator.provideAppConfig()
  implicit val dbLayer = new DatabaseLayer(PostgresProfile)

  val routing = new MainRoute()
  routing.setupRouting(appCfg.host, appCfg.port)

  logger.info(s"Server started at http://${appCfg.host}:${appCfg.port}")
}

