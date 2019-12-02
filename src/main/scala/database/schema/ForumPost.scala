package database.schema

import java.time.Instant

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}
import CustomColumnTypes._
import FieldsValueClasses._

case class ForumPost(
                      topic: Topic,
                      content: Content,
                      nickname: Nickname,
                      email: Option[String] = None,
                      secret: Secret,
                      timestamp: Instant,
                      id: PK[ForumPost] = PK[ForumPost](0L)
                    )

class ForumPostTable(tag: Tag) extends Table[ForumPost](tag, "ForumPost") {

  def topic: Rep[Topic] = column[Topic]("topic")

  def content: Rep[Content] = column[Content]("content")

  def nickname: Rep[Nickname] = column[Nickname]("nickname")

  def email: Rep[Option[String]] = column[Option[String]]("email")

  def secret: Rep[Secret] = column[Secret]("secret")

  def timestamp: Rep[Instant] = column[Instant]("timestamp")

  def id: Rep[PK[ForumPost]] = column[PK[ForumPost]]("post_id", O.PrimaryKey, O.AutoInc)

  def * : ProvenShape[ForumPost] = (topic, content, nickname, email, secret, timestamp, id) <> (ForumPost.tupled, ForumPost.unapply)
}

object ForumPostOps {

  lazy val posts = TableQuery[ForumPostTable]
  lazy val insertPost = posts returning posts.map(_.id.value)

}




