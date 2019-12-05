package database.layer.modules

import java.time.Instant

import database.Profile
import database.schema.FieldsValueClasses.{Content, Nickname, Secret, Topic}
import database.schema.{ForumPost, PK}
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait ForumPostModule {
  self: Profile =>

  val profile: JdbcProfile

  import database.schema.CustomColumnTypes._
  import profile.api._

  class ForumPostTable(tag: Tag) extends Table[ForumPost](tag, "ForumPost") {

    def topic: Rep[Topic] = column[Topic]("topic")

    def content: Rep[Content] = column[Content]("content")

    def nickname: Rep[Nickname] = column[Nickname]("nickname")

    def email: Rep[Option[String]] = column[Option[String]]("email")

    def secret: Rep[Secret] = column[Secret]("secret")

    def createTs: Rep[Instant] = column[Instant]("create_ts")

    def updateTs: Rep[Instant] = column[Instant]("update_ts")

    def id: Rep[PK[ForumPost]] = column[PK[ForumPost]]("post_id", O.PrimaryKey, O.AutoInc)

    def * : ProvenShape[ForumPost] = (topic, content, nickname, email, secret, createTs, updateTs, id) <> (ForumPost.tupled, ForumPost.unapply)
  }

  lazy val posts = TableQuery[ForumPostTable]
  lazy val insertPost = posts returning posts.map(_.id.value)

  def exec[T](action: DBIO[T]): Future[T]

  def selectAllPosts() = exec(
    posts.result
  )

  def insertNewPost(newPost: ForumPost) = exec(
    insertPost += newPost
  )

  def insertManyPosts(posts: ForumPost*) = exec(
    insertPost ++= posts
  )

  def findPost(id: Long) = exec(
    posts.filter(_.id === PK[ForumPost](id)).result.headOption
  )

  def updatePostContent(id: Long, newContent: String) = exec(
    posts
      .filter(_.id === PK[ForumPost](id))
      .map(_.content)
      .update(Content(newContent))
  )

  def updatePostTimestamp(id: Long, updateTs: Instant) = exec(
    posts
      .filter(_.id === PK[ForumPost](id))
      .map(_.updateTs)
      .update(updateTs)
  )

  def deletePost(id: Long) = exec (
    posts.filter(_.id === PK[ForumPost](id)).delete
  )

  def getTopPosts(limit: Int, offset: Int) = exec (
    posts
      .sortBy(_.updateTs.desc)
      .drop(offset)
      .take(limit)
      .result
  )


}
