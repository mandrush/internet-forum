package route

import java.time.Instant

import database.schema.FieldsValueClasses._
import database.schema.ForumPost
import db.DatabaseSetup
import domain.logic.ForumJSONSupport
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

class NewPostSpec extends WordSpec with Matchers with ForumJSONSupport with ScalaFutures with DatabaseSetup with BeforeAndAfter {

  before {
    createSchema()
  }

  val newPost = ForumPost(Topic("a"), Content("b"), Nickname("s"), Some("asd@asd.sl"), Secret("123"), Instant.now)

  "test" should {

    "asd" in {
      val insert = dbLayer.insertNewPost(newPost)
      Await.result(insert, 5.seconds)
    }

  }


}
