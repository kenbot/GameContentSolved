package kenbot.gcsolved.core
package io

import java.io.File
import scala.collection.JavaConverters._
import java.util.{Map => JMap}
import javax.xml.validation.Schema


object Datafier {
  
  type DataMap = Map[String, Any]
  type DataSeq = Seq[Any]
  private val mapType = "__maptype__"

  trait DataTranslation[A] {
    type Data
    def toData(a: A): Data
    def fromData(d: Data): A
  }
  
  class TranslationContext(schema: Schema) {
    implicit object FieldTranslation extends DataTranslation[Field] {
      type Data = DataMap
      
      def toData(field: Field): Data = Map(
        mapType -> "field",
        "name" -> field.name,
        "fieldType" -> field.fieldType.name,
        "category" -> field.category,
        "required" -> field.required,
        "isId" -> field.isId,
        "default" -> anyToData(field.default),
        "description" -> field.description)
        
      def fromData(data: Data): Field = {
        val name = data("name")
        val fieldType = schema findObject anyFromData(data("fieldType"))
        val category = data("category").asInstanceOf[String]
        val required = data("required").asInstanceOf[Boolean]
        val isId = data("isId").asInstanceOf[Boolean]
        val default = data("default")
        val description = data("description").asInstanceOf[Boolean]
      }
    }
  }
    
  object ObjectDataMap {
    def unapply(map: DataMap): Option[DataMap] = stripDataMap(map, "object")
  }  
    
  object AnyDataMap {
    def unapply(map: DataMap): Option[DataMap] = stripDataMap(map, "any")
  }   
  
  object JustAMap {
    def unapply(map: DataMap): Option[DataMap] = stripDataMap(map, "map")
  }
  
  private def stripDataMap(map: DataMap, expectedType: String): Option[DataMap] = 
    if (map(mapType) == expectedType) Some(map - mapType)
    else None
    
  private implicit def provideCastToApprovedDataTypes(a: Any) = new {
    def toDataMap: DataMap = a.asInstanceOf[DataMap]
    def toDataSeq = a.asInstanceOf[DataSeq]
  }

  def objectDataToData(objectData: ObjectData): DataMap = Map(
    mapType -> "object",
    "type" -> objectData.resourceType.name,
    "fields" -> mapToData(objectData.fields))
  
  def mapToData[A](data: Map[String,A]): DataMap = (data mapValues anyToData) + (mapType -> "map")
  def seqToData[A](seq: Seq[A]): DataSeq = seq map anyToData
  def anyDataToData(anyData: AnyData): DataMap = Map(
    mapType -> "any",
    "resourceType" -> anyData.resourceType.name,
    "value" -> anyToData(anyData.value))
  
  def anyToData(value: Any): Any = value match {
    case data: ObjectData => objectDataToData(data)
    case anyData: AnyData => anyDataToData(anyData)
    case field: Field => fieldToData(field)
    case map:  Map[String @unchecked,_] => mapToData(map)
    case seq: Seq[_] => seqToData(seq)
    case f: File => f.getName
    case whatever => whatever.toString
  }
    
  def fieldToData(field: Field): DataMap = Map(
    mapType -> "field",
    "name" -> field.name,
    "fieldType" -> field.fieldType.name,
    "category" -> field.category,
    "required" -> field.required,
    "isId" -> field.isId,
    "default" -> anyToData(field.default),
    "description" -> field.description)

  def libraryToData(lib: ResourceLibrary): DataMap = Map(
    mapType -> "library",
    "id" -> lib.id,
    "name" -> lib.name,
    "description" -> lib.description,
    "localResources" -> seqToData(lib.localResources.toSeq),
    "linkedLibraries" -> seqToData(lib.linkedLibraries.map(_.name)))
  
  
  def anyFromData(data: Any, schema: ResourceSchema): Any = data match {
    case ObjectDataMap(map) => objectDataFromData(map, schema)
    case AnyDataMap(map) => anyDataFromData(map)
    case JustAMap(map) => map
    case seq: DataSeq =>
    case str: String => 
    case x => sys.error(s"Expecting a string, map or seq, found: $x")
  }
  
  def objectDataFromData(map: DataMap, schema: ResourceSchema): ObjectData = {
    val typeName = map("type").toString
    val resourceType = schema findObjectType typeName
    val fields = map("fields").toDataMap
    for ((key, field: String) <- fields) {
      //fromData()
      
    }
    ???
  }
  
  def anyDataFromData(map: DataMap): ObjectData = ???

  def libraryFromData(data: DataMap, schema: ResourceSchema): ResourceLibrary = {
    val id = data("id").toString
    val name = data("name").toString
    val description = data("description").toString
    val localResources: DataSeq = data("localResources").toDataSeq
    val linkedLibraries: Seq[ResourceLibrary] = ???
    
    ResourceLibrary(data("id").toString, data("name").toString, schema).
      updateDescription(data("description").toString).
      addResources(seqFromData(localResources, schema).asInstanceOf[Seq[RefData]]: _*).
      addLinkedLibraries(linkedLibraries: _*)
  }

  def seqFromData[A](seq: DataSeq, schema: ResourceSchema): Seq[Any] = 
    seq.map(stuff => anyFromData(stuff, schema))
  
}

