package database.layer.modules

import database.Profile
import database.schema.FieldsValueClasses.Topic
import database.schema.ForumPost
import slick.jdbc.JdbcProfile

trait ForumPostModule { self : Profile =>

  val profile: JdbcProfile

  import database.schema.CustomColumnTypes._
  import database.schema.ForumPostOps._
  import profile.api._

  def insertNewPost(newPost: ForumPost) = {
    insertPost += newPost
  }

  def findPostWithTopic(topic: String) = posts.filter(_.topic === Topic(topic)).result.headOption

}
