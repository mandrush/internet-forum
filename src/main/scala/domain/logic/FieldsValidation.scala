package domain.logic

import akka.http.scaladsl.server.Directives.validate
import akka.http.scaladsl.server.Route
import database.schema.FieldsValueClasses._
import org.apache.commons.validator.routines.EmailValidator
import route.Routes.ContemporaryConfig

trait FieldsValidation {

  def validateFields(email: Option[String], nickname: Nickname, content: Content)
                    (inner: => Route)
                    (implicit cfg: ContemporaryConfig): Route = {

    validate(validateEmail(email), "Email cannot be malformed!") {

      validate(checkField(nickname, cfg.minLen, cfg.maxNick), s"Nickname needs to have length between ${cfg.minLen} and ${cfg.maxNick} characters") {

          validate(checkField(content, cfg.minLen, cfg.maxContent), s"Content needs to have beetween ${cfg.minLen} and ${cfg.maxContent} characters!") {
            inner
          }
      }
    }
  }
  //  using regex for this task is a BAD idea
  //    see https://stackoverflow.com/questions/201323/how-to-validate-an-email-address-using-a-regular-expression
  private def validateEmail(email: Option[String]): Boolean = email.forall(EmailValidator.getInstance().isValid(_))

  def checkField(field: Requested, minLen: Int, maxLen: Int): Boolean = field.inner.length >= minLen && field.inner.length <= maxLen

}