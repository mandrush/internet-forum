package db

import database.layer.DatabaseLayer
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.duration._

import H2Profile.api._

trait DatabaseSetup {

  implicit val dbLayer = new DatabaseLayer(H2Profile)

  def dropTables() = {
    val dropFP = dbLayer.exec(sql"""DROP TABLE IF EXISTS "ForumPost" CASCADE""".as[Long])
    val dropFR = dbLayer.exec(sql"""DROP TABLE IF EXISTS "ForumReply" CASCADE""".as[Long])
    Await.result(dropFP, 2.seconds)
    Await.result(dropFR, 2.seconds)
  }

  def setupDb() = {
    dropTables()

    val create = dbLayer.createSchema
    Await.result(create, 2.seconds)
  }

//  def simulateDBMalfunction() = {
//    val dropFP = dbLayer.exec(sql"""DROP TABLE IF EXISTS "ForumPost" CASCADE""".as[Long])
//    val dropFR = dbLayer.exec(sql"""DROP TABLE IF EXISTS "ForumReply" CASCADE""".as[Long])
//    Await.result(dropFP, 2.seconds)
//    Await.result(dropFR, 2.seconds)
//
//  }

}
