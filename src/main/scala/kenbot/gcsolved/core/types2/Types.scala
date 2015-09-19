package kenbot.gcsolved.core

import scalaz._, Scalaz._

package object types2 {

  sealed trait GPrimitive[A]

  type Name = String

  type GString = GString.type
  type GBool = GBool.type
  type GInt = GBool.type

  sealed trait GType[A]
  case object GInt extends GType[Int]
  case object GString extends GType[String]
  case object GBool extends GType[Boolean]
  case class GList[A](elementType: GType[A]) extends GType[List[A]]
  case class GObject(fields: NonEmptyList[(Name, GType[_])]) extends GType[Map[Name,_]]










}



