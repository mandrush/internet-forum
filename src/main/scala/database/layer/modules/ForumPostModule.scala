package database.layer.modules

import java.time.Instant

import database.Profile
import database.schema.ForumPost
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

trait ForumPostModule { self: Profile =>

  val profile: JdbcProfile

  import profile.api._
  import database.schema.ForumPostOps._

  def insertNewPost(newPost: ForumPost) = {
    insertPost += newPost
  }

}
