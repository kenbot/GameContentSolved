name := "GameContentSolved"

version := "0.1"

organization := "kenbot"

scalaVersion := "2.10.1"

sbtVersion := "0.12"

resolvers += "Sonatype-public" at "http://oss.sonatype.org/content/groups/public/"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-swing" % "2.10.1",
  "org.yaml" % "snakeyaml" % "1.12",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "com.github.benhutchison" % "scalaswingcontrib" % "1.5",
  "junit" % "junit" % "4.8.1" % "test")

initialCommands := """
    import kenbot.gcsolved.core._;
    import kenbot.gcsolved.core.types._;
    import scala.swing._;
    import Swing._;
  """


