name := "scala-akka-http"

version := "0.1"

scalaVersion := "2.13.0"

libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.9"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.23" // or whatever the latest version is
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.9"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"
libraryDependencies += "org.scanamo" %% "scanamo" % "1.0.0-M10"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.23",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.9"
)
