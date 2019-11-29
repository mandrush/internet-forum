package database.schema

import java.time.Instant

import database.schema.CustomColumnTypes._
import database.schema.FieldsValueClasses._
import slick.jdbc.PostgresProfile.api._
import slick.model.ForeignKeyAction.Cascade

case class ForumReply(
                       content: Content,
                       nickname: Nickname,
                       email: Option[String],
                       timestamp: Instant,
                       parentId: PK[ForumPost],
                       id: PK[ForumReply] = PK[ForumReply](0L)
                     )

import database.schema.ForumPostOps._

class ForumReplyTable(tag: Tag) extends Table[ForumReply](tag, "ForumReply") {
  def content = column[Content]("content")

  def nickname = column[Nickname]("nickname")

  def email = column[Option[String]]("email")

  def timestamp = column[Instant]("timestamp")

  def parentId = column[PK[ForumPost]]("parent_id")

  def id = column[PK[ForumReply]]("reply_id")

  override def * = (content, nickname, email, timestamp, parentId, id) <> (ForumReply.tupled, ForumReply.unapply)

  def parent = foreignKey("post_fk", parentId, posts)(_.id, onDelete = Cascade, onUpdate = Cascade)

}

private[database] object ForumReplyOps {
  lazy val replies = TableQuery[ForumReplyTable]
  lazy val insertReply = replies returning replies.map(_.id.value)
}
