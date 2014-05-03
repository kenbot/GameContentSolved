package kenbot.gcsolved.core.types
import kenbot.gcsolved.core.ObjectData
import kenbot.gcsolved.core.Field
import scala.collection.SortedMap
import scala.collection.mutable.LinkedHashMap
import scala.collection.immutable.ListMap
import kenbot.gcsolved.core.ResourceRef
import kenbot.gcsolved.core.meta._

abstract class ObjectType private[core] (name: String, 
    parentType: => ObjectType, 
    val isReferenceType: Boolean, 
    override val isAbstract: Boolean,
    objectFields: => Seq[Field]) extends ResourceType(name, parentType) with UserType {
  
  
  protected type MyType <: ObjectType 
  type MyData <: ObjectData
  
  lazy val localFields: Map[Field.Name, Field] = {
    ListMap(initFields(objectFields).map(f => (f.name,f)): _*)
  }
  
  protected def initFields(theLocalFields: Seq[Field]): Seq[Field] = theLocalFields
  
  def fields: Map[Field.Name, Field] = if (parent eq this) localFields 
                                       else ListMap() ++ parent.fields ++ localFields
                      
  def abstractly: MyType
  def extend(parent: => MyType): MyType
  def defines(fields: Field*): MyType
  def definesLazy(fields: => Seq[Field]): MyType        
  
  
  lazy val categories: Seq[Field.Name] = fields.values.map(_.category).filter(_.nonEmpty).toSeq
  
  override lazy val parent: ObjectType = parentType
  
  def getFieldType(fieldName: Field.Name): ResourceType = {
    require(fields contains fieldName, toString + " does not have field " + fieldName)
    fields(fieldName).fieldType
  }
  
  def emptyData: MyData
  def apply(fields: (Field.Name, Any)*): MyData
  
  private def equality = (name, parent.name, isReferenceType, isAbstract, localFields)
  override def hashCode() = equality.hashCode
  override def equals(a: Any) = a match {
    case o: ObjectType => equality == o.equality
    case _ => false
  }
}
