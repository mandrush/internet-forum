package route

import java.time.Instant

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import database.schema.FieldsValueClasses.{Content, Nickname, Secret, Topic}
import database.schema.ForumPost
import db.DatabaseSetup
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import domain.PathNames._
import domain.logic.ForumJSONSupport
import route.pagination.TopPostsRoute

import scala.concurrent.duration._
import scala.concurrent.Await

class TopPostsSpec extends WordSpec with ScalatestRouteTest with DatabaseSetup with BeforeAndAfterAll with Matchers with ScalaFutures with ForumJSONSupport {

  val path = "/" + GetTopPosts

  implicit val cCfg = Configurator.provideAppConfig()

  val topPostRoute = TopPostsRoute.topPostsRoute

  override protected def beforeAll(): Unit = {
    setupDb()

    val posts = Seq(
      ForumPost(Topic("leastrecent"), Content("ASD"), Nickname("@@@@"), Some("halo@jasio.pl"), Secret("123"), Instant.now, updateTs = Instant.ofEpochMilli(1500000001000L)),
      ForumPost(Topic("grzesio"), Content("ASD"), Nickname("@@@@"), Some("halo@jasio.pl"), Secret("123"), Instant.now, updateTs = Instant.ofEpochMilli(1500000002000L)),
      ForumPost(Topic("misio"), Content("ASD"), Nickname("@@@@"), Some("halo@jasio.pl"), Secret("123"), Instant.now, updateTs = Instant.ofEpochMilli(1500000003000L)),
      ForumPost(Topic("marek"), Content("ASD"), Nickname("@@@@"), Some("halo@jasio.pl"), Secret("123"), Instant.now, updateTs = Instant.ofEpochMilli(1500000004000L)),
      ForumPost(Topic("darek"), Content("ASD"), Nickname("@@@@"), Some("halo@jasio.pl"), Secret("123"), Instant.now, updateTs = Instant.ofEpochMilli(1500000005000L)),
      ForumPost(Topic("lala"), Content("ASD"), Nickname("@@@@"), Some("halo@jasio.pl"), Secret("123"), Instant.now, updateTs = Instant.ofEpochMilli(1500000006000L)),
      ForumPost(Topic("tinkywinky"), Content("ASD"), Nickname("@@@@"), Some("halo@jasio.pl"), Secret("123"), Instant.now, updateTs = Instant.ofEpochMilli(1500000007000L)),
      ForumPost(Topic("mostrecent"), Content("ASD"), Nickname("@@@@"), Some("halo@jasio.pl"), Secret("123"), Instant.now, updateTs = Instant.ofEpochMilli(1500000009000L))
    )
    Await.result(dbLayer.insertManyPosts(posts: _*), 2.seconds)
  }

  s"Server processing GET request sent to $path" should {
    "respond with 200 OK and a proper page when there's a given offset and limit" in {
      val limit = 3
      val offset = 0
      Get(s"$path?limit=$limit&offset=$offset") ~> Route.seal(topPostRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        val toplist = Await.result(Unmarshal(responseEntity).to[Seq[ForumPost]], 2.seconds)

        toplist.head.topic.value shouldBe "mostrecent"
        toplist.size shouldBe 3
      }

      val limit2 = 3
      val offset2 = 3
      Get(s"$path?limit=$limit2&offset=$offset2") ~> Route.seal(topPostRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        val toplist = Await.result(Unmarshal(responseEntity).to[Seq[ForumPost]], 2.seconds)

        toplist.head.topic.value shouldBe "darek"
        toplist.size shouldBe 3
      }


      val limit3 = 3
      val offset3 = 6
      Get(s"$path?limit=$limit3&offset=$offset3") ~> Route.seal(topPostRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        val toplist = Await.result(Unmarshal(responseEntity).to[Seq[ForumPost]], 2.seconds)

        toplist.head.topic.value shouldBe "grzesio"
        toplist.size shouldBe 2
      }
    }

    s"respond with 200 OK and the page size should be limited by max pagination limit config when the 'limit' param exceeds the configured value" in {
      val limit = 100
      val offset = 0
      Get(s"$path?limit=$limit&offset=$offset") ~> Route.seal(topPostRoute) ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        val toplist = Await.result(Unmarshal(responseEntity).to[Seq[ForumPost]], 2.seconds)

        toplist.head.topic.value shouldBe "mostrecent"
        toplist.size shouldBe cCfg.maxPaginationLimit
      }
    }

    "respond with 400 Bad Request when any of the parameters is malformed" in {
      val limit = "asdf"
      val offset = 0
      Get(s"$path?limit=$limit&offset=$offset") ~> Route.seal(topPostRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      val limit2 = "asdf"
      val offset2 = "qwe"
      Get(s"$path?limit=$limit2&offset=$offset2") ~> Route.seal(topPostRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
      val limit3 = 2
      val offset3 = "qwe"
      Get(s"$path?limit=$limit3&offset=$offset3") ~> Route.seal(topPostRoute) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }
  }
}


