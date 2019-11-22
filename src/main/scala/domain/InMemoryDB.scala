package domain

import domain.Domain.ForumPost

import scala.collection.mutable.ListBuffer

object InMemoryDB {

  val posts = new ListBuffer[ForumPost]

}
