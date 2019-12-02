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

//  final case class UserEditPost(
//                                 newContent: Option[String],
//                                 secret: Option[String]
//                               )

}
