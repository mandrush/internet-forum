package domain.logic.fields

object RequestFields {

  trait Requested extends Any {
    def inner: Option[String]
  }

  case class Email   (value: Option[String]) extends AnyVal with Requested {
    override def inner: Option[String] = value
  }
  case class Topic   (value: Option[String]) extends AnyVal with Requested {
    override def inner: Option[String] = value
  }
  case class Nickname(value: Option[String]) extends AnyVal with Requested {
    override def inner: Option[String] = value
  }
  case class Content (value: Option[String]) extends AnyVal with Requested {
    override def inner: Option[String] = value
  }
  case class Secret  (value: Option[String]) extends AnyVal with Requested {
    override def inner: Option[String] = value
  }

}
