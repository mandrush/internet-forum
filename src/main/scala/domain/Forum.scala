package domain

object Forum {
//todo tu by sie przydala jakas hierarchia klas, jakis polimorfizm - nie udalo sie tego teraz napisac, bo ze sprayem byly problemy
  final case class ForumPost(id: Int,
                             topic: Option[String],
                             content: Option[String],
                             nickname: Option[String],
                             email: Option[String])

  final case class ForumResponse(parent: ForumPost,
                                 content: Option[String],
                                 nickname: Option[String],
                                 email: Option[String])

  final case class BasicForumEntity(content: Option[String],
                                    nickname: Option[String],
                                    email: Option[String])

}
