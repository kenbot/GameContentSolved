package kenbot.gcsolved.resource

import kenbot.gcsolved.resource.types.ObjectType
import scala.annotation.tailrec

trait ObjectData {
  require(!resourceType.isAbstract, 
      "Cannot directly instantiate abstract resource type: " + resourceType)
  
  def id = ""

  def resourceType: ObjectType
  def fields: Map[Field.Name, Any]
  def valid: Boolean = failures.isEmpty
  
  def matches(searchString: String): Boolean
  def updateField(field: Field.Name, value: Any): ObjectData  
  def apply[A](field: Field.Name): A = fields(field).asInstanceOf[A]
  def getOrElse[A](field: Field.Name, orElse: A): A = fields.getOrElse(field, orElse).asInstanceOf[A]
  def get[A](field: Field.Name): Option[A] = fields.get(field).map(_.asInstanceOf[A])
  def refersTo(resourceRef: ResourceRef): Boolean = externalRefs contains resourceRef
  
  def updateResourceRefs(resourceRef: ResourceRef, newId: String): ObjectData
  
  def empty = fields.isEmpty
  
  def debugString: String
  
  def failures: List[String] = resourceType.fields.flatMap {
    case (name, Field(fieldType, _, _)) => fieldType.getFailures(fields(name))
  }.toList
  
  protected def getFieldsWithUpdatedResourceRefs(resourceRef: ResourceRef, newId: String): Map[Field.Name, Any] = {
    def updateValue(a: Any): Any = a match {
      case `resourceRef` => resourceRef.copy(id = newId)
      case list: List[_] => list map updateValue
      case map: Map[_,_] => map map {
        entry => updateValue(entry._1) -> updateValue(entry._2)
      }
      case data: ValueData => data.updateResourceRefs(resourceRef, newId)
      case other => other
    }
    
    fields mapValues updateValue
  }
  
  
  def externalRefs: Seq[ResourceRef] = {
    def findExternalRefs(a: Any): Seq[ResourceRef] = a match {
      case r: ResourceRef => Seq(r)
      case list: List[Any] => list flatMap findExternalRefs
      case map: Map[_,_] => map.values.toSeq flatMap findExternalRefs
      case data: ValueData => data.externalRefs
      case _ => Seq()
    }
    fields.values.toSeq flatMap findExternalRefs
  }
}
