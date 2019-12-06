import sbt._

object Dependencies {

  val akka = Seq(
    "com.typesafe.akka" %% "akka-http" % "10.1.10",
    "com.typesafe.akka" %% "akka-actor" % "2.6.0",
    "com.typesafe.akka" %% "akka-stream" % "2.6.0",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.10"
  )

  val tests = Seq(
    "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.0",
    "com.typesafe.akka" %% "akka-http-testkit" % "10.1.10",
    "org.scalatest" %% "scalatest" % "3.0.8" % "test",
    "com.h2database" % "h2" % "1.4.200" % Test
  )

  val commonsLang3 = Seq(
    "commons-validator" % "commons-validator" % "1.4.0",
    "org.apache.commons" % "commons-lang3" % "3.9"
  )

  val slick = "com.typesafe.slick" %% "slick" % "3.3.2"

  val postgres = "org.postgresql" % "postgresql" % "42.2.8"

  val logback = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )


}
