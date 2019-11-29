import java.time.Instant

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import database.layer.DatabaseLayer
import database.schema.FieldsValueClasses.{Content, Nickname, Secret, Topic}
import database.schema.{ForumPost, ForumReply, PK}
import slick.jdbc.PostgresProfile

import scala.concurrent.Await
import scala.concurrent.duration._


object Server extends App {

  private implicit val system = ActorSystem("4chan")
  private implicit val materializer = ActorMaterializer()
  private implicit val ec = system.dispatcher

  import route.Routes._

  val bindingFuture = Http().bindAndHandle(mainRoute, "localhost", 8080)
  val dbLayer = new DatabaseLayer(PostgresProfile)

  val insert = dbLayer.insertNewPost(ForumPost(Topic("a"), Content("bv"), Nickname("c"), Some("dddd2@wp.pl"), Secret("e"), Instant.now))
  print(Await.result(dbLayer.db.run(insert), 1.seconds))

  val insertReply = dbLayer.insertNewReply(ForumReply(Content("dddd"), Nickname("aAAA"), None, Instant.now, PK[ForumPost](1)))
  print(Await.result(dbLayer.db.run(insertReply), 2.seconds))

}

