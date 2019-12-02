package domain.request

object UserRequests {

  final case class UserCreatePost(
                                   topic: String,
                                   content: String,
                                   nickname: String,
                                   email: Option[String]
                                 )


  final case class UserReply(
                              content: String,
                              nickname: String,
                              email: Option[String]
                            )

  final case class UserEdit(
                             newContent: String,
                             secret: String
                           )

}
