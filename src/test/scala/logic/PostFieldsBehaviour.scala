package logic

import akka.http.scaladsl.testkit.ScalatestRouteTest
import domain.logic.ForumJSONSupport
import org.scalatest.{Matchers, WordSpec}

trait PostFieldsBehaviour extends WordSpec with Matchers with ScalatestRouteTest with ForumJSONSupport {
  val path: String


}
