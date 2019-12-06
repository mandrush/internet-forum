package database.layer.modules

import java.time.Instant

import database.Profile
import database.schema.FieldsValueClasses.{Content, Nickname, Secret}
import database.schema.{ForumPost, ForumReply, PK}
import slick.jdbc.JdbcProfile
import slick.model.ForeignKeyAction.Cascade

import scala.concurrent.Future

trait ForumReplyModule {
  self: Profile with ForumPostModule =>

  val profile: JdbcProfile

  import database.schema.CustomColumnTypes._
  import profile.api._

  class ForumReplyTable(tag: Tag) extends Table[ForumReply](tag, "ForumReply") {
    def content = column[Content]("content")

    def nickname = column[Nickname]("nickname")

    def email = column[Option[String]]("email")

    def timestamp: Rep[Instant] = column[Instant]("timestamp")

    def secret = column[Secret]("secret")

    def parentId = column[PK[ForumPost]]("parent_id")

    def id = column[PK[ForumReply]]("reply_id", O.PrimaryKey, O.AutoInc)

    override def * = (content, nickname, email, timestamp, secret, parentId, id) <> (ForumReply.tupled, ForumReply.unapply)

    def parent = foreignKey("post_fk", parentId, posts)(_.id, onDelete = Cascade, onUpdate = Cascade)

  }

  lazy val replies = TableQuery[ForumReplyTable]
  lazy val insertReply = replies returning replies.map(_.id.value)


  def exec[T](action: DBIO[T]): Future[T]

  def selectAllReplies() = exec(
    replies.result
  )

  def insertNewReply(newReply: ForumReply) = exec(
    insertReply += newReply
  )

  def insertManyReplies(replies: ForumReply*) = exec(
    insertReply ++= replies
  )

  def findReply(id: Long) = exec(
    replies.filter(_.id === PK[ForumReply](id)).result.headOption
  )

  def updateReplyContent(id: Long, newContent: String) = exec(
    replies
      .filter(_.id === PK[ForumReply](id))
      .map(_.content)
      .update(Content(newContent))
  )

  def deleteReply(id: Long) = exec (
    replies.filter(_.id === PK[ForumReply](id)).delete
  )

  def repliesToPost(postId: Int) = exec(
    (replies.filter(_.parentId === PK[ForumPost](postId))
      join posts
      on (_.parentId === _.id))
      .sortBy(_._1.timestamp.asc)
      .result
  )

}

