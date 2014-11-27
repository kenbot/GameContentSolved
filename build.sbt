name := "GameContentSolved"

version := "0.1"

organization := "kenbot"

scalaVersion := "2.10.4"

sbtVersion := "0.13"

resolvers += "Sonatype-public" at "http://oss.sonatype.org/content/groups/public/"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-swing" % "2.10.4",
  "org.yaml" % "snakeyaml" % "1.12",
  "org.scalatest" %% "scalatest" % "2.1.3" % "test",
  "com.github.benhutchison" % "scalaswingcontrib" % "1.5",
  "junit" % "junit" % "4.8.1" % "test")

initialCommands := """
    import kenbot.gcsolved.core._;
    import kenbot.gcsolved.core.types._;
    import scala.swing._;
    import Swing._;
  """


