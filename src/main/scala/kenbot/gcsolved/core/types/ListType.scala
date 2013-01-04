package kenbot.gcsolved.core.types

import kenbot.gcsolved.core.meta._

object ListType {
  val name = "List"
}

final case class ListType(val elementType: ResourceType, 
                          val maxLength: Option[Int] = None) extends ResourceType(ListType.name + "(" + elementType.name + ")") {
  
  type Element = elementType.Value
  type Value = List[Element]
  
  def metaType: MetaAnyType = MetaListType
  
  def asValue(a: Any): Value = {
    a.asInstanceOf[List[Any]] map elementType.asValue
  }
  
  override def <:<(other: ResourceType): Boolean = other match {
    case AnyType => true
    case other: ListType => 
      def elementTypeConforms = elementType <:< other.elementType
      def maxLengthConforms = (maxLength, other.maxLength) match {
        case (Some(ourMax), Some(theirMax)) => ourMax <= theirMax
        case (None, Some(_)) => false
        case (_, None) => true
      }
      elementTypeConforms && maxLengthConforms
      
    case _ => false
  }
  
  
  override def getFailures(value: Any): List[String] = {
    value match {
      case list: List[Any] => list flatMap elementType.getFailures
      case x => super.getFailures(value)
    }
  }
}