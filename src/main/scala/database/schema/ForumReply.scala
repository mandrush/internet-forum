package database.schema

import java.time.Instant

import database.schema.FieldsValueClasses._

case class ForumReply(
                       content: Content,
                       nickname: Nickname,
                       email: Option[String],
                       timestamp: Instant,
                       secret: Secret,
                       parentId: PK[ForumPost],
                       id: PK[ForumReply] = PK[ForumReply](0L)
                     )


