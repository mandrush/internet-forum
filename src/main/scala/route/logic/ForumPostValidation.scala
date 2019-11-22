package route.logic

import akka.http.scaladsl.server.Directive0
import domain.Domain.ForumPost
import akka.http.scaladsl.server.Directives.validate

trait ForumPostValidation {
//chyba StandardRoute powinno zwracac
//  def validateForumPost(post: Option[ForumPost]): StandardRoute = {
//    validate(validateEmail(post.flatMap(_.email)), "Email was wrong or not defined!") {
//
//    }
//  }

  def validateEmail(email: Option[String]): Boolean = {

    val emailRegex = """^.*?\@.*?\.\w+$""".r

    email match {
      case Some(e) => emailRegex.findFirstMatchIn(e).isDefined
      case None    => false
    }
  }

}
