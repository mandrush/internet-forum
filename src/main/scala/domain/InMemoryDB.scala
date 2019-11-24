package domain

import domain.forum.entity.Forum.ForumPost

import scala.collection.mutable.ListBuffer

object InMemoryDB {

  val posts = new ListBuffer[ForumPost]

  def findPostWithGivenId(id: Int): Option[ForumPost]= {
    posts.find(_.id == id)
  }

}
