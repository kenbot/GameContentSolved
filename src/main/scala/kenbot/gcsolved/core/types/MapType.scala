package kenbot.gcsolved.core.types

import kenbot.gcsolved.core.meta._

object MapType {
  val name = "Map"
}

final case class MapType(val keyType: SelectOneType, val valueType: ResourceType) 
    extends ResourceType("Map(%s, %s)".format(keyType.name, valueType.name)) {
  
  def metaType: MetaAnyType = MetaMapType
  
  type Value = Map[keyType.Value, valueType.Value]
  
  def asValue(a: Any): Value = a.asInstanceOf[Map[Any, Any]] map {
    case (k,v) => (keyType asValue k, valueType asValue v)
  }

  override def getFailures(value: Any): List[String] = {
    value match {
      case map: Map[_, _] => 
        (map.keys.toList flatMap keyType.getFailures) ++ (map.values.toList flatMap valueType.getFailures)
        
      case x => super.getFailures(value)
    }
  }
  
}