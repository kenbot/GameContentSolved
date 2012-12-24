package kenbot.gcsolved.core.types

import kenbot.gcsolved.core.meta._

class StringType(val maxLength: Option[Int]) extends ResourceType("String") {
  type Value = String
  
  require(maxLength.forall(_ > 0), "Max length must be greater than zero")
  
  def metaType: MetaAnyType = MetaStringType
  
  override def <:<(other: ResourceType): Boolean = other match {
   case AnyType => true
   case other: StringType => (maxLength, other.maxLength) match {
      case (Some(ourMaxLength), Some(theirMaxLength)) => ourMaxLength <= theirMaxLength
      case (None, Some(_)) => false
      case (_, None) => true
    }
   case _ => false
  }
  
  def asValue(a: Any) = a.toString
}

case object StringType extends StringType(None) {
  def apply(maxLength: Option[Int]) = new StringType(maxLength)
  
  def unapply(res: ResourceType): Option[Option[Int]] = res match {
    case s: StringType => Some(s.maxLength)
    case _ => None
  }
}

