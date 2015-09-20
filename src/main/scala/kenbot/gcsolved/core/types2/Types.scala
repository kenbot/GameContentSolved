package kenbot.gcsolved.core

import scalaz._, Scalaz._

package object types2 {

  sealed trait GPrimitive[A]

  type Name = String

  case class Fix[F[_]](unfix: F[Fix[F]])

  type GTypeRec = GType[Fix[GType]]

  object GTypeRec {

    def int: GTypeRec = GType.int
    def string: GTypeRec = GType.string 
    def bool: GTypeRec = GType.bool
    def list(elementType: GTypeRec): GTypeRec = GType.list(Fix(elementType))
    def obj(head: (Name, GTypeRec), tail: (Name, GTypeRec)*): GTypeRec = 
      GType.obj((head._1, Fix(head._2)), tail.map(kv => (kv._1, Fix(kv._2))): _*) 
  }


  sealed trait GType[+A] {
    import GType._

    def fold[B](
        ifInt: => B, 
        ifString: => B, 
        ifBool: => B, 
        ifList: A => B, 
        ifObject: NonEmptyList[(Name, A)] => B): B = {

      this match {
        case GInt => ifInt
        case GString => ifString
        case GBool => ifBool
        case GList(a) => ifList(a)  
        case GObject(fields) => ifObject(fields) 
      }
    }

    def map[B](f: A => B): GType[B] = 
      fold(GInt, GString, GBool, 
        a => GList(f(a)), 
        fields => GObject(fields.map(kv => (kv._1, f(kv._2)))))
  }


  object GType {
    def int[A]: GType[A] = GInt
    def string[A]: GType[A] = GString 
    def bool[A]: GType[A] = GBool
    def list[A](elementType: A): GType[A] = GList(elementType)
    def obj[A](headField: (Name, A), tailFields: (Name, A)*): GType[A] = 
      GObject(NonEmptyList(headField, tailFields: _*))

    private case object GInt extends GType[Nothing]
    private case object GString extends GType[Nothing]
    private case object GBool extends GType[Nothing]
    private case class GList[A](elementType: A) extends GType[A]
    private case class GObject[A](fields: NonEmptyList[(Name, A)]) extends GType[A]
  }









}



