package kenbot.gcsolved.resource.types
import kenbot.gcsolved.resource.meta.MetaAnyType
import kenbot.gcsolved.resource.meta.MetaRefType
import kenbot.gcsolved.resource.Field
import kenbot.gcsolved.resource.RefData
import kenbot.gcsolved.resource.ResourceRef

object AnyRefType extends RefType("AnyRef", isAbstract=true) {
  
  override lazy val localFields: Map[Field.Name, Field] = Map.empty
  override protected def initFields(theLocalFields: Seq[Field]): Seq[Field] = theLocalFields
  override def fields: Map[Field.Name, Field] = Map.empty
  
  override def getFailures(value: Any): List[String] = {
    if (value.isInstanceOf[ResourceRef]) Nil
    else List("Expecting a ResourceRef: " + value)
  }
}

object RefType {

  def recursive(name: String, fields: => Seq[Field]): RefType = recursive(name, AnyRefType, false, fields)
  def recursive(name: String, parent: => RefType, isAbstract: Boolean, fields: => Seq[Field]): RefType = {
    new RefType(name, parent, isAbstract, fields)
  }
    
  def apply(name: String, fields: Field*): RefType = apply(name, AnyRefType, false, fields: _*)
  def apply(name: String, parent: => RefType, isAbstract: Boolean, fields: Field*): RefType = {
    new RefType(name, parent, isAbstract, fields)
  }
  
  def unapply(resourceType: ResourceType): Option[String] = resourceType match {
    case rt: RefType => Some(rt.name)
    case _ => None
  }
}

class RefType(
    name: String, 
    parentType: => RefType = AnyRefType, 
    override val isAbstract: Boolean = false,
    theFields: => Seq[Field] = Seq()) 
    
  extends ObjectType(name, parentType, true, false, theFields) {
  
  def metaType: MetaAnyType = MetaRefType
  type Value = ResourceRef
  
  def idField: Field.Name = fields.values.find(_.isId) match {
    case Some(field) => field.name
    case None => error("RefType " + name + " doesn't have an ID field!")
  }
  
  override protected def initFields(theLocalFields: Seq[Field]): Seq[Field] = {
    def idCount = {
      val allFields = if (parent eq this) theLocalFields 
                      else theLocalFields ++ parent.fields.values
      allFields.count(_.isId)
    }
                    
    require(isAbstract || idCount == 1, "RefType " + name + " must have exactly one ID field: " + idCount)
    theLocalFields
  }
  
  override lazy val parent: RefType = parentType
  
  override def emptyData = {
    val defaultValues = fields.values.map(f => (f.name, f.default))
    val definedDefaultsOnly = defaultValues.collect { 
      case (name, Some(default)) => (name, default) 
    }
    RefData(this, definedDefaultsOnly.toMap)
  }
   
  override def asValue(a: Any): Value = a.asInstanceOf[ResourceRef]
  
  override def getFailures(a: Any): List[String] = a match {
    case ref: ResourceRef => if (ref.refType <:< this) Nil
                             else List("Expected a " + name + " reference, found: " + ref.refType.name)
    case x => List("Expecting a ResourceRef, found: " + x)
  }
}