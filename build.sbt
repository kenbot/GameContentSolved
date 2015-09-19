name := "GameContentSolved"

version := "0.1"

organization := "kenbot"

scalaVersion := "2.11.7"

resolvers += "Sonatype-public" at "http://oss.sonatype.org/content/groups/public/"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-swing" % "2.0.0-M2",
  "org.scalaz" %% "scalaz-effect" % "7.1.3",
  "org.scalaz" %% "scalaz-core" % "7.1.3",
  "org.yaml" % "snakeyaml" % "1.12",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.github.benhutchison" % "scalaswingcontrib" % "1.5",
  "junit" % "junit" % "4.8.1" % "test")

initialCommands := """
    import kenbot.gcsolved.core._;
    import kenbot.gcsolved.core.types._;
    import scala.swing._;
    import Swing._;
  """


