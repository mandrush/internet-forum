package database.layer

import database.Profile
import database.layer.modules.{ForumPostModule, ForumReplyModule}
import slick.jdbc.JdbcProfile

class DatabaseLayer(val profile: JdbcProfile) extends
  Profile with
  ForumPostModule with
  ForumReplyModule {
  override val jdbcProfile = profile

  val db = profile.backend.Database.forConfig("database")

}
