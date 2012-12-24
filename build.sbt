name := "GameContentSolved"

version := "0.1"

organization := "kenbot"

scalaVersion := "2.9.2"

sbtVersion := "0.13"


libraryDependencies += "org.scala-lang" % "scala-swing" % "2.9.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.6.1" % "test"

libraryDependencies += "com.github.benhutchison" % "scalaswingcontrib" % "1.3"

libraryDependencies += "junit" % "junit" % "4.8.1" % "test"

scalacOptions += "-Ydependent-method-types"

initialCommands := """
    import kenbot.gcsolved.resource._;
    import kenbot.gcsolved.resource.types._;
    import scala.swing._;
    import Swing._;
  """
