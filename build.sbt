name := "hovedOpgave"
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  guice,
  "com.typesafe.play" %% "play-slick" % "4.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.0",
  "mysql" % "mysql-connector-java" % "8.0.15",
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.0" % "test",
)

libraryDependencies += "com.github.t3hnar" %% "scala-bcrypt" % "4.1"

