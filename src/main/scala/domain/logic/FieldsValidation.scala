package domain.logic

import akka.http.scaladsl.server.Directives.validate
import akka.http.scaladsl.server.Route
import org.apache.commons.validator.routines.EmailValidator

trait FieldsValidation {

//  value classes would be great here, as a potential developer wouldn't be able to mistake one field with another (all are of type Option[String])
//  however, I ran into some problems with spray-json not serializing the fields as I would like it to so no value classes for now
  def validateFields(email: Option[String], nickname: Option[String], content: Option[String])
                    (inner: => Route): Route = {

    validate(validateEmail(email), "Email cannot be empty or malformed!") {

      validate(checkNickname(nickname), "Nickname needs to have length between 1 and 21 characters") {

          validate(checkContent(content), "Content needs to have beetween 1 and 400 characters!") {
            inner
          }
      }
    }
  }

  private def validateEmail(email: Option[String]): Boolean = {
//  using regex for this task is a BAD idea
//    see https://stackoverflow.com/questions/201323/how-to-validate-an-email-address-using-a-regular-expression
    email match {
      case Some(e) => EmailValidator.getInstance().isValid(e)
      case None    => false
    }
  }

  import FieldsValidation._
  private def checkNickname(nick: Option[String]): Boolean = nick match {
    case Some(n) => if (n.length < minimumLength || n.length > maxNickLength) false else true
    case None    => false
  }

  def checkTopic(topic: Option[String]): Boolean = topic match {
    case Some(t) => if (t.length < minimumLength || t.length > maxTopicLength) false else true
    case None    => false
  }

  private def checkContent(content: Option[String]): Boolean = content match {
    case Some(c) => if (c.length < minimumLength || c.length > maxContentLength) false else true
    case None    => false
  }
}

object FieldsValidation {
  val maxNickLength    = 21
  val maxTopicLength   = 80
  val maxContentLength = 400
  val minimumLength    = 1
}
