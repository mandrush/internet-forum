scalaVersion := "2.13.1"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.10"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.0"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.0"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.10"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.0",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.10"
)

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"
libraryDependencies += "commons-validator" % "commons-validator" % "1.4.0"
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.9"