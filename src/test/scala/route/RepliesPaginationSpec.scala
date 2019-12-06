package route

import java.time.Instant

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import database.schema.FieldsValueClasses.{Content, Nickname, Secret, Topic}
import database.schema.{ForumPost, ForumReply, PK}
import db.DatabaseSetup
import domain.PathNames._
import domain.logic.ForumJSONSupport
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import route.pagination.RepliesPaginationRoute._

import scala.concurrent.Await
import scala.concurrent.duration._

class RepliesPaginationSpec extends WordSpec with ScalatestRouteTest with Matchers with BeforeAndAfterAll with ForumJSONSupport with DatabaseSetup with ScalaFutures {

  var postId = 0L
  var replyId = 0L

  val path = "/" + GetPaginatedReplies
  implicit val appCfg = AppConfig()
  val pivotTimestamp = Instant.ofEpochMilli(1500000004000L)
  val pivotMarker = "chosen one"

  override protected def beforeAll(): Unit = {
    setupDb()

    val mainPost = ForumPost(Topic("leastrecent"), Content("ASD"), Nickname("@@@@"), Some("halo@jasio.pl"), Secret("123"), Instant.now, updateTs = Instant.ofEpochMilli(1500000004000L))
    postId = Await.result(dbLayer.insertNewPost(mainPost), 2.seconds).value

    val givenReply = ForumReply(Content(pivotMarker), Nickname("strzala"), Some("asd@wp.pl"), pivotTimestamp, Secret("asd"), PK[ForumPost](postId))
    replyId = Await.result(dbLayer.insertNewReply(givenReply), 2.seconds).value



    val others = Seq(
      ForumReply(Content("oldest"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000000000L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000001000L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000002000L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000003000L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000004500L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000004600L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000005100L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000005200L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000006000L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000007000L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000008000L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000009000L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000009100L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000009200L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000009300L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000009400L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000009500L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000009600L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000009700L), Secret("asd"), PK[ForumPost](postId)),
      ForumReply(Content("asd"), Nickname("strzala"), Some("asd@wp.pl"), Instant.ofEpochMilli(1500000009800L), Secret("asd"), PK[ForumPost](postId))
    )
    Await.result(dbLayer.insertManyReplies(others: _*), 2.seconds)

  }

  s"Server processing GET requests to $path" should {
    "respond with 200 OK and a correctly paginated list when before & after exceed pagination limit" in {
      val before = 4
      val after = 12
      Get(s"$path?post_id=$postId&reply_id=$replyId&before=$before&after=$after") ~> Route.seal(repliesPaginatedRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        val paginated = Await.result(Unmarshal(responseEntity).to[Seq[ForumReply]], 2.seconds)

        paginated.size shouldBe appCfg.maxPaginationLimit
        paginated.map(_.content.value).indexOf(pivotMarker) shouldBe 2

        paginated.splitAt(2)._1.length shouldBe 2
        paginated.splitAt(2)._2.length shouldBe 5

      }
    }

    "respond with 200 OK and a correctly paginated list when the limit is not exceeded (after < before)" in {
      val before = 1
      val after = 2
      Get(s"$path?post_id=$postId&reply_id=$replyId&before=$before&after=$after") ~> Route.seal(repliesPaginatedRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        val paginated = Await.result(Unmarshal(responseEntity).to[Seq[ForumReply]], 2.seconds)

        paginated.size shouldBe 4
        paginated.map(_.content.value).indexOf(pivotMarker) shouldBe 1

      }
    }

    "respond with 200 OK and a correctly paginated list when the limit is not exceeded (after > before)" in {
      val before = 2
      val after = 1
      Get(s"$path?post_id=$postId&reply_id=$replyId&before=$before&after=$after") ~> Route.seal(repliesPaginatedRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        val paginated = Await.result(Unmarshal(responseEntity).to[Seq[ForumReply]], 2.seconds)

        paginated.size shouldBe 4
        paginated.map(_.content.value).indexOf(pivotMarker) shouldBe 2

      }
    }

    "respond with 404 Not Found when no such post has been found" in {
      Get(s"$path?post_id=123123123&reply_id=$replyId&before=1&after=2") ~> Route.seal(repliesPaginatedRoute) ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "respond with 404 Not Found when no such reply has been found" in {
      Get(s"$path?post_id=$postId&reply_id=123123123&before=1&after=2") ~> Route.seal(repliesPaginatedRoute) ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "respond with 400 Bad Request when parameter path is malformed" in {
      Get(s"$path?post_id=a&reply_id=1&before=asd2&after=3a") ~> Route.seal(repliesPaginatedRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }
  }



}
