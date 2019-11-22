
import Domain.Item
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol
import spray.json.DefaultJsonProtocol._
object Domain {

  final case class Item(id: Int, decr: String)
//  jsonFormatX gdzie X - ile jest pol w case klasie
//  https://doc.akka.io/docs/akka-http/current/common/json-support.html

}

trait JSONSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val itemFormat = jsonFormat2(Item)
}
