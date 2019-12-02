package database.layer

import database.Profile
import database.layer.modules.{ForumPostModule, ForumReplyModule}
import slick.dbio.DBIO
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class DatabaseLayer(val profile: JdbcProfile) extends
  Profile with
  ForumReplyModule with
  ForumPostModule {

  override val jdbcProfile = profile

  val db = profile.backend.Database.forConfig("database")

  def exec[T](action: DBIO[T]): Future[T] = db.run(action)

}
