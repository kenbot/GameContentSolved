name := "GameContentSolved"

version := "0.1"

organization := "kenbot"

scalaVersion := "2.9.1"

sbtVersion := "0.11"


libraryDependencies += "org.scala-lang" % "scala-swing" % "2.9.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.6.1" % "test"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0.3"

libraryDependencies += "Ken Scambler" % "scalaswingtreewrapper_2.9.1" % "1.2"

libraryDependencies += "junit" % "junit" % "4.8.1" % "test"

scalacOptions += "-Ydependent-method-types"

initialCommands := """
    import kenbot.gcsolved.resource._;
    import kenbot.gcsolved.resource.types._;
    import scala.swing._;
    import Swing._;
  """
