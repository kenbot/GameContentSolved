package kenbot.gcsolved.core
import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.core.types.ValueType
import kenbot.gcsolved.core.types.ResourceType
import kenbot.gcsolved.core.types.SelectOneType
import kenbot.gcsolved.core.types.ObjectType
import kenbot.gcsolved.core.types.AnyRefType
import kenbot.gcsolved.core.types.AnyValueType
import kenbot.gcsolved.core.types.AnyType
import kenbot.gcsolved.core.types.UserType


object ResourceSchema {
  def apply() = new ResourceSchema(Seq(), Seq(), Seq(), Seq())
  def apply( 
      theRefTypes: => Seq[RefType],
      theValueTypes: => Seq[ValueType],
      theSelectOneTypes: => Seq[SelectOneType]) = {
    
    new ResourceSchema(theRefTypes, theValueTypes, theSelectOneTypes, Seq())
  }
  
  val Empty = ResourceSchema()
}

class ResourceSchema private (  
    theRefTypes: => Seq[RefType],
    theValueTypes: => Seq[ValueType],
    theSelectOneTypes: => Seq[SelectOneType],
    val linkedSchemas: Seq[ResourceSchema]) {
  
  top => 
  
  def refTypes: Seq[RefType] = AnyRefType :: userRefTypes
  def valueTypes: Seq[ValueType] = AnyValueType :: userValueTypes
  def selectOneTypes: Seq[SelectOneType] = userSelectOneTypes
    
  def objectTypes: List[ObjectType] = userRefTypes ++ userValueTypes
  lazy val userRefTypes: List[RefType] = theRefTypes.filterNot(_.name == AnyRefType.name).toList
  lazy val userValueTypes: List[ValueType] = theValueTypes.filterNot(_.name == AnyValueType.name).toList
  lazy val userSelectOneTypes: List[SelectOneType] = theSelectOneTypes.toList
  
  private lazy val refTypeMap: Map[String, RefType] = refTypes.map(t => t.name -> t).toMap
  private lazy val valueTypeMap: Map[String, ValueType] = valueTypes.map(t => t.name -> t).toMap
  private lazy val selectOneTypeMap: Map[String, SelectOneType] = selectOneTypes.map(t => t.name -> t).toMap
  

  def findSelectOneType(name: String): Option[SelectOneType] = {
    val resType = selectOneTypeMap.get(name) 
    (resType /: linkedSchemas)(_ orElse _.findSelectOneType(name))
  }
  
  def findValueType(name: String): Option[ValueType] = {
    val resType = valueTypeMap.get(name) 
    (resType /: linkedSchemas)(_ orElse _.findValueType(name))
  }
  
  def findRefType(name: String): Option[RefType] = {
    val resType = refTypeMap.get(name) 
    (resType /: linkedSchemas)(_ orElse _.findRefType(name))
  }
  
  def findObjectType(name: String): Option[ObjectType] = findRefType(name) orElse findValueType(name)
  
  def addSelectOneTypes(selectOneTypes: SelectOneType*) = copy(selectOneTypes = addToSeq(this.userSelectOneTypes, selectOneTypes))
  def addValueTypes(valueTypes: ValueType*) = copy(valueTypes = addToSeq(this.userValueTypes, valueTypes))
  def addRefTypes(refTypes: RefType*) = copy(refTypes = addToSeq(this.userRefTypes, refTypes))

  def addLinkedSchema(schema: ResourceSchema) = {
    // TODO disallow cyclic linking
    copy(linkedSchemas = linkedSchemas :+ schema)
  }
  
  private def copy(
      refTypes: Seq[RefType] = this.userRefTypes, 
      valueTypes: Seq[ValueType] = this.userValueTypes,
      selectOneTypes: Seq[SelectOneType] = this.userSelectOneTypes,
      linkedSchemas: Seq[ResourceSchema] = this.linkedSchemas) = {
    
    new ResourceSchema(refTypes, valueTypes, selectOneTypes, linkedSchemas)
  }
  
  private def addToSeq[R <: ResourceType](seq: Seq[R], resourceTypes: Seq[R]) = {
    resourceTypes foreach {rt => 
      val msg = "ResourceType with name \"" + rt.name + " already found!"
      require(!refTypeMap.contains(rt.name), msg)
      require(!valueTypeMap.contains(rt.name), msg)
      require(!selectOneTypeMap.contains(rt.name), msg)
    }
    seq ++ resourceTypes
  }
  
  private def equality = (refTypeMap, valueTypeMap, selectOneTypeMap)
  override def hashCode() = equality.hashCode
  
  override def toString() = "ResourceSchema" + (refTypes.toSet, valueTypes.toSet, selectOneTypes.toSet).toString
  
  override def equals(a: Any): Boolean = a match {
    case other: ResourceSchema => equality == other.equality
    case _ => false
  }
}