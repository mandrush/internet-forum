package database.schema

import java.time.Instant

import database.schema.FieldsValueClasses._

case class ForumPost(
                      topic: Topic,
                      content: Content,
                      nickname: Nickname,
                      email: Option[String] = None,
                      secret: Secret,
                      timestamp: Instant,
                      id: PK[ForumPost] = PK[ForumPost](0L)
                    )





