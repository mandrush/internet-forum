package domain.logic

import akka.http.scaladsl.server.Directives.validate
import akka.http.scaladsl.server.Route
import database.schema.FieldsValueClasses._
import org.apache.commons.validator.routines.EmailValidator
import route.MainRoute.ContemporaryConfig
import spray.json.JsValue

trait FieldsValidation {

  def validateFields(email: Option[String], nickname: Nickname, content: Content)
                    (inner: => Route)
                    (implicit cfg: ContemporaryConfig): Route = {

    validate(checkEmail(email), "Email cannot be malformed!") {

      validateField(nickname, cfg.minLen, cfg.maxNick) {

        validateField(content, cfg.minLen, cfg.maxContent) {
          inner
        }
      }
    }
  }

  def validateField(field: Requested, minLen: Int, maxLen: Int)
                   (inner: => Route)
                   (implicit cfg: ContemporaryConfig): Route = {
    validate(checkField(field, minLen, maxLen), s"${field.getClass.getSimpleName} length needs to be between $minLen and $maxLen characters") {
      inner
    }
  }

  //  using regex for this task is a BAD idea
  //    see https://stackoverflow.com/questions/201323/how-to-validate-an-email-address-using-a-regular-expression
  private def checkEmail(email: Option[String]): Boolean = email.forall(EmailValidator.getInstance().isValid(_))

  private def checkField(field: Requested, minLen: Int, maxLen: Int): Boolean = field.inner.length >= minLen && field.inner.length <= maxLen

  def onlyContains(req: JsValue, fields: String*): Boolean = req.asJsObject.fields.keySet equals fields.toSet

}