package db

import database.layer.DatabaseLayer
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.duration._

trait DatabaseSetup {

  import H2Profile.api._
  val dbLayer = new DatabaseLayer(H2Profile)

  def createSchema() = {
    val create = dbLayer.createSchema
    Await.result(create, 2.seconds)
    println("DB is setup")
  }


}
