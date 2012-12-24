package kenbot.gcsolved.core.types
import kenbot.gcsolved.core.meta._
import kenbot.gcsolved.core.ResourceRef

object SelectOneType {
  def apply[A](name: String, valueType: ResourceType { type Value = A }, values: A*): SelectOneType = 
    new SelectOneType(name, valueType, values)
}

final class SelectOneType private (
    name: String,  
    val valueType: ResourceType,  
    valueList: Seq[Any]) extends ResourceType(name, AnyType) with UserType {
  
  
  type Value = valueType.Value
  
  val values: Seq[Value] = valueList.toList map valueType.asValue
  
  def metaType: MetaAnyType = MetaRefType
  
  override def getFailures(value: Any): List[String] = value match {
    case v if valueType acceptsValue v => 
      if (values contains value) Nil
      else List("Value must be one of " + values.mkString(", ") + ": " + value)
      
    case _ => super.getFailures(value)
  }
  
  private def equality = (name, valueType, values)
  
  override def asValue(a: Any): Value = valueType asValue a
  override def hashCode() = equality.hashCode
  override def equals(a: Any) = a match {
    case o: SelectOneType => equality == o.equality
    case _ => false
  }
}