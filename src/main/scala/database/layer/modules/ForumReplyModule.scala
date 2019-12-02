package database.layer.modules

import database.Profile
import database.schema.FieldsValueClasses.Content
import database.schema.{ForumReply, PK}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

trait ForumReplyModule {
  self: Profile =>

  val profile: JdbcProfile

  import profile.api._
  import database.schema.ForumReplyOps._

  def exec[T](action: DBIO[T]): Future[T]

  def insertNewReply(newReply: ForumReply) = exec(
    insertReply += newReply
  )

  def findReply(id: Long) = exec(
    replies.filter(_.id === PK[ForumReply](id)).result.headOption
  )

  def updateReply(id: Long, newContent: String) = exec(
    replies
      .filter(_.id === PK[ForumReply](id))
      .map(_.content)
      .update(Content(newContent))
  )


}
