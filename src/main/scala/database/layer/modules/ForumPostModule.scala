package database.layer.modules

import database.Profile
import database.schema.FieldsValueClasses.{Content, Topic}
import database.schema.{ForumPost, PK}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

trait ForumPostModule {
  self: Profile =>

  val profile: JdbcProfile

  import database.schema.CustomColumnTypes._
  import database.schema.ForumPostOps._
  import profile.api._

  def exec[T](action: DBIO[T]): Future[T]

  def insertNewPost(newPost: ForumPost) = exec(
    insertPost += newPost
  )

  def findPost(id: Long) = exec(
    posts.filter(_.id === PK[ForumPost](id)).result.headOption
  )

  def updatePost(id: Long, newContent: String) = exec(
    posts
      .filter(_.id === PK[ForumPost](id))
      .map(_.content)
      .update(Content(newContent))
  )

  def deletePost(id: Long) = exec (
    posts.filter(_.id === PK[ForumPost](id)).delete
  )
}
