import Dependencies._

scalaVersion := "2.13.1"

parallelExecution in Test := false

libraryDependencies ++= akka
libraryDependencies ++= tests
libraryDependencies ++= commonsLang3
libraryDependencies += slick
libraryDependencies += postgres