package kenbot.gcsolved.resource

import kenbot.gcsolved.resource.types.RefType
import ResourceLibrary.ResourceLibraryRef

object RefData {
  def apply(resourceType: RefType, fields: (Field.Name, Any)*): RefData = apply(resourceType, fields.toMap)
  def apply(resourceType: RefType, fields: Map[Field.Name, Any]): RefData = new RefData(resourceType, 0, None, fields)
}

class RefData private (
  val resourceType: RefType,
  val version: Int,
  val definedIn: Option[ResourceLibraryRef],
  givenFields: Map[Field.Name, Any]) extends ObjectData {

  override lazy val id: String = get(resourceType.idField).map(_.toString) getOrElse ""
  
  val fields: Map[Field.Name, Any] = resourceType.defaultValueMap ++ givenFields
  
  private def copy(resourceType: RefType = this.resourceType,
    version: Int = this.version,
    definedIn: Option[ResourceLibraryRef] = this.definedIn,
    fields: Map[Field.Name, Any] = this.fields) = new RefData(resourceType, version, definedIn, fields)
   
  def ref = ResourceRef(id, resourceType) 

  def matches(searchString: String): Boolean = {
    val s = searchString.toLowerCase
    (id.toString.toLowerCase contains s) || 
    (resourceType.name.toLowerCase contains s) ||
    fields.valuesIterator.exists(_.toString.toLowerCase contains s)
  }

  private def isFieldDefined(field: Field): Boolean = fields contains field.name
  
  override def valid: Boolean = fields.contains(resourceType.idField) && id.toString != "" && super.valid
  
  def incrementVersion(): RefData = copy(version = version+1)
  def updateId(newId: Any) = updateField(resourceType.idField, newId)
  def asDefinedIn(externalLibrary: ResourceLibrary) = copy(definedIn = Some(externalLibrary.id))
  def isDefinedIn(externalLibrary: ResourceLibrary): Boolean = definedIn.map(externalLibrary.ref ==) getOrElse false 

  def updateResourceRefs(resourceRef: ResourceRef, newId: String): RefData = {
    val updatedFields = getFieldsWithUpdatedResourceRefs(resourceRef, newId)
    copy(fields = updatedFields)
  }

  protected def updateFields(newFields: Map[Field.Name, Any]) = copy(fields = newFields)
  def updateField(field: Field.Name, value: Any): RefData = copy(fields = fields + (field -> value))
  
  private def equality = (id, resourceType, fields)
  
  override def hashCode() = equality.hashCode
  
  override def equals(a: Any) = a match {
    case r: RefData => equality == r.equality
    case _ => false
  }
  
  
  override def toString() = id
  def debugString = "RefData(%s, %s, %s)".format(id, resourceType, fields)
}
