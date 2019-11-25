package domain.logic

import akka.http.scaladsl.server.Directives.validate
import akka.http.scaladsl.server.Route
import domain.logic.fields.RequestFields.{Content, Email, Nickname, Requested}
import org.apache.commons.validator.routines.EmailValidator
import route.Routes.ContemporaryConfig

//todo: tu najlepiej bedzie dodac config z pliku z maksymalnymi dlugosciami postow contentu itd
//todo: i wtedy przekazywac to jako implicit do validate fields

trait FieldsValidation {

  def validateFields(email: Email, nickname: Nickname, content: Content)
                    (inner: => Route)
                    (implicit cfg: ContemporaryConfig): Route = {

    validate(validateEmail(email), "Email cannot be empty or malformed!") {

      validate(checkField(nickname, cfg.minLen, cfg.maxNick), "Nickname needs to have length between 1 and 21 characters") {

          validate(checkField(content, cfg.minLen, cfg.maxContent), "Content needs to have beetween 1 and 400 characters!") {
            inner
          }
      }
    }
  }

  private def validateEmail(email: Email): Boolean = {
//  using regex for this task is a BAD idea
//    see https://stackoverflow.com/questions/201323/how-to-validate-an-email-address-using-a-regular-expression
    email.value match {
      case Some(e) => EmailValidator.getInstance().isValid(e)
      case None    => false
    }
  }

  def checkField(field: Requested, minLen: Int, maxLen: Int): Boolean = field.inner match {
    case Some(f) => if (f.length < minLen || f.length > maxLen) false else true
    case None    => false
  }



}