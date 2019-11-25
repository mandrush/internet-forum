package domain.forum

//todo tu by sie przydala jakas hierarchia klas, jakis polimorfizm - nie udalo sie tego teraz napisac, bo ze sprayem byly problemy
//todo: w sumie nie bylo to az tak konieczne, ale
//todo byc moze sie przyda
//trait BasicForumLike {
//  val content: Content
//  val email: Email
//  val nickname: Nickname
//}

object Forum {

  final case class ForumPost(
                              id: Int,
                              topic: Option[String],
                              content: Option[String],
                              nickname: Option[String],
                              email: Option[String]
                            )

  final case class ForumResponse(
                                  id: Int,
                                  parent: ForumPost,
                                  content: Option[String],
                                  nickname: Option[String],
                                  email: Option[String]
                                )

  final case class EditedResponse(
                                   edited: ForumResponse,
                                   secret: Option[String],
                                   content: Option[String],
                                   email: Option[String],
                                   nickname: Option[String]
                                 )

}
