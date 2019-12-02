package database.layer.modules

import database.Profile
import database.schema.FieldsValueClasses.{Content, Topic}
import database.schema.{ForumPost, PK}
import slick.jdbc.JdbcProfile

trait ForumPostModule {
  self: Profile =>

  val profile: JdbcProfile

  import database.schema.CustomColumnTypes._
  import database.schema.ForumPostOps._
  import profile.api._

  def insertNewPost(newPost: ForumPost) = {
    insertPost += newPost
  }

  def findPost(id: Long) = posts.filter(_.id === PK[ForumPost](id)).result.headOption

  def updatePost(id: Long, newContent: String) =
    posts
      .filter(_.id === PK[ForumPost](id))
      .map(_.content)
      .update(Content(newContent))

}
