name := "scala-akka-http"

version := "0.1"

scalaVersion := "2.12.9"

val scanamoVersion = "1.0.0-M10"

libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.9"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.23" // or whatever the latest version is
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.9"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.23" % Test

libraryDependencies += "org.mockito" %% "mockito-scala" % "1.5.13" % Test

libraryDependencies += "org.scanamo" %% "scanamo" % scanamoVersion
libraryDependencies += "org.scanamo" %% "scanamo-alpakka" % scanamoVersion
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.23",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.9"
)
