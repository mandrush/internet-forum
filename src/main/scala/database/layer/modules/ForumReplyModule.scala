package database.layer.modules

import database.Profile
import database.schema.FieldsValueClasses.Content
import database.schema.{ForumReply, PK}
import slick.jdbc.JdbcProfile

trait ForumReplyModule { self : Profile =>

  val profile: JdbcProfile

  import profile.api._
  import database.schema.ForumReplyOps._

  def insertNewReply(newReply: ForumReply) = insertReply += newReply

  def findReply(id: Long) = replies.filter(_.id === PK[ForumReply](id)).result.headOption

  def updateReply(id: Long, newContent: String) =
    replies
      .filter(_.id === PK[ForumReply](id))
      .map(_.content)
      .update(Content(newContent))


}
