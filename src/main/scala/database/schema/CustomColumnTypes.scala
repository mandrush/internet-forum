package database.schema

import slick.jdbc.PostgresProfile.api._

case class PK[A](value: Long) extends AnyVal with MappedTo[Long]

object CustomColumnTypes {

  import FieldsValueClasses._

  implicit val forumPostPKColumnType: BaseColumnType[PK[ForumPost]]   = MappedColumnType.base[PK[ForumPost], Long] (_.value, PK[ForumPost])
  implicit val forumReplyPKColumnType: BaseColumnType[PK[ForumReply]] = MappedColumnType.base[PK[ForumReply], Long] (_.value, PK[ForumReply])

  implicit val topicType: BaseColumnType[Topic]       = MappedColumnType.base[Topic, String] (_.value, Topic)

  implicit val contentType: BaseColumnType[Content]   = MappedColumnType.base[Content, String] (_.value, Content)

  implicit val nicknameType: BaseColumnType[Nickname] = MappedColumnType.base[Nickname, String] (_.value, Nickname)

  implicit val secretType: BaseColumnType[Secret]     = MappedColumnType.base[Secret, String] (_.value, Secret)

  //  todo: i guess you could also do it like this to avoid too much code?
  //  implicit val requestFieldType = MappedColumnType.base[Requested, String] (
  //    reqtd => reqtd.inner,
  //    str   => new Requested {
  //      override def inner: String = str
  //    }
  //  )
}

