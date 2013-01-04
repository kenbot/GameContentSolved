package kenbot.gcsolved.core.types
import kenbot.gcsolved.core.meta.MetaAnyType
import kenbot.gcsolved.core.meta.MetaRefType
import kenbot.gcsolved.core.Field
import kenbot.gcsolved.core.RefData
import kenbot.gcsolved.core.ResourceRef

object AnyRefType extends RefType("AnyRef", isAbstract=true) {
  
  override lazy val localFields: Map[Field.Name, Field] = Map.empty
  override protected def initFields(theLocalFields: Seq[Field]): Seq[Field] = theLocalFields
  override def fields: Map[Field.Name, Field] = Map.empty

  override lazy val defaultValueMap: Map[Field.Name, Any] = Map.empty 
  
  override def getFailures(value: Any): List[String] = {
    if (value.isInstanceOf[ResourceRef]) Nil
    else List("Expecting a ResourceRef: " + value)
  }

  override def equals(a: Any) = a match {
    case x: AnyRef if x eq this => true
    case _ => false
  }
}

object RefType {
  
  def apply(name: String): RefType = new RefType(name)
/*
  def recursive(name: String, fields: => Seq[Field]): RefType = recursive(name, AnyRefType, false, fields)
  def recursive(name: String, parent: => RefType, isAbstract: Boolean, fields: => Seq[Field]): RefType = {
    new RefType(name, parent, isAbstract, fields)
  }
    
  def apply(name: String, fields: Field*): RefType = apply(name, AnyRefType, false, fields: _*)
  def apply(name: String, parent: => RefType, fields: Field*): RefType = new RefType(name, parent, false, fields)
  def apply(name: String, parent: => RefType, isAbstract: Boolean, fields: Field*): RefType = {
    new RefType(name, parent, isAbstract, fields)
  }*/
  
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
  protected type MyType = RefType
  
  def idField: Field.Name = fields.values.find(_.isId) match {
    case Some(field) => field.name
    case None => error("RefType " + name + " doesn't have an ID field!")
  }
  
  def abstractly = new RefType(name, parent, true, theFields)
  def extend(parent: => RefType) = new RefType(name, parent, isAbstract, theFields)
  def defines(fields: Field*) = definesLazy(fields)
  def definesLazy(fields: => Seq[Field]) = new RefType(name, parentType, isAbstract, theFields ++ fields)
  
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
  
  override def emptyData = RefData(this)
  
  lazy val defaultValueMap: Map[Field.Name, Any] = (for {
    field <- fields.values
    default <- field.default
  } 
  yield field.name -> default).toMap
  
  override def asValue(a: Any): Value = a.asInstanceOf[ResourceRef]
  
  override def getFailures(a: Any): List[String] = a match {
    case ref: ResourceRef => if (ref.refType <:< this) Nil
                             else List("Expected a " + name + " reference, found: " + ref.refType.name)
    case x => List("Expecting a ResourceRef, found: " + x)
  }
}
