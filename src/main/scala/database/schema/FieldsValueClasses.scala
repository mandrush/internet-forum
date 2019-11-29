package database.schema

import slick.lifted.MappedTo

object FieldsValueClasses {

  trait Requested extends Any {
    def inner: String
  }

  case class Topic   (value: String) extends AnyVal with Requested with MappedTo[String] {
    override def inner: String = value
  }
  case class Nickname(value: String) extends AnyVal with Requested with MappedTo[String] {
    override def inner: String = value
  }
  case class Content (value: String) extends AnyVal with Requested with MappedTo[String] {
    override def inner: String = value
  }
  case class Secret  (value: String) extends AnyVal with Requested with MappedTo[String] {
    override def inner: String = value
  }

}
