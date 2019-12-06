package database.schema

import slick.jdbc.PostgresProfile.api._

case class PK[A](value: Long) extends AnyVal with MappedTo[Long]

object CustomColumnTypes {

  import FieldsValueClasses._

  implicit val forumPostPKColumnType: BaseColumnType[PK[ForumPost]] = MappedColumnType.base[PK[ForumPost], Long](_.value, PK[ForumPost])
  implicit val forumReplyPKColumnType: BaseColumnType[PK[ForumReply]] = MappedColumnType.base[PK[ForumReply], Long](_.value, PK[ForumReply])

  implicit val requestFieldType = MappedColumnType.base[Requested, String](
    reqtd => reqtd.inner,
    str => new Requested {
      override def inner: String = str
    }
  )
}

