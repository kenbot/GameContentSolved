package kenbot.gcsolved.resource
import kenbot.gcsolved.resource.types.ValueType

object ValueData {
  def apply(resourceType: ValueType, fields: (Field.Name, Any)*): ValueData = apply(resourceType, fields.toMap)
  def apply(resourceType: ValueType, fields: Map[Field.Name, Any]): ValueData = new ValueData(resourceType, fields)
  
  def unapply(valueData: Any): Option[(ValueType, Map[Field.Name, Any])] = valueData match {
    case vd: ValueData => Some(vd.resourceType, vd.fields)
    case _ => None
  }
}

class ValueData private (
  val resourceType: ValueType,
  fieldValues: Map[Field.Name, Any]) extends ObjectData {
   
  val fields: Map[Field.Name, Any] = Map() ++ fieldValues
  
  private def copy(
    resourceType: ValueType = this.resourceType,
    fields: Map[Field.Name, Any] = this.fields) = new ValueData(resourceType, fields)
   
  def matches(searchString: String): Boolean = {
    val s = searchString.toLowerCase
    (resourceType.name.toLowerCase contains s) ||
    fields.valuesIterator.exists(_.toString.toLowerCase contains s)
  }

  def updateResourceRefs(resourceRef: ResourceRef, newId: String): ValueData = {
    val updatedFields = getFieldsWithUpdatedResourceRefs(resourceRef, newId)
    copy(fields = updatedFields)
  }
  
  def updateField(field: Field.Name, value: Any): ValueData = copy(fields = fields + (field -> value))
  
  private def equality = (resourceType, fields)
  
  override def hashCode() = equality.hashCode

  override def equals(a: Any) = a match {
    case r: ValueData => equality == r.equality
    case _ => false
  }
  
  override def toString(): String = fields.map(kv => kv._1.toString + ": " + kv._2.toString).mkString("{", ", ", "}")
  def debugString: String = "ValueData(%s, %s)".format(resourceType, fields)
}
