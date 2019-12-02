package database.layer.modules

import database.Profile
import database.schema.ForumReply
import slick.jdbc.JdbcProfile

trait ForumReplyModule { self : Profile =>

  val profile: JdbcProfile

  import profile.api._
  import database.schema.ForumReplyOps._

  def insertNewReply(newReply: ForumReply) = insertReply += newReply

}
