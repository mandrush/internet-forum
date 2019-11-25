package domain.request

object UserRequests {

  final case class UserCreatePost(
                                   topic: Option[String],
                                   content: Option[String],
                                   nickname: Option[String],
                                   email: Option[String]
                                 )

  final case class UserCostam(
                                   topic: Option[String],
                                   content: Option[String],
                                   nickname: Option[String],
                                   email: Option[String]
                                 )

  final case class UserReply(
                              content: Option[String],
                              nickname: Option[String],
                              email: Option[String]
                            )

  final case class UserEditPost(
                                 newContent: Option[String],
                                 secret: Option[String]
                               )

}
