package kenbot.gcsolved.resource.types
import scala.annotation.migration
import kenbot.gcsolved.resource.Field
import kenbot.gcsolved.resource.ValueData
import kenbot.gcsolved.resource.ResourceRef
import kenbot.gcsolved.resource.meta._

object AnyValueType extends ValueType("AnyValue", isAbstract=true)  {
  
  override def fields: Map[Field.Name, Field] = Map.empty
  
  override def getFailures(value: Any): List[String] = {
    if (value.isInstanceOf[ValueData]) Nil
    else List("Expecting a ResourceRef: " + value)
  }
}
  
object ValueType {
  def apply(name: String, fields: Field*): ValueType = apply(name, AnyValueType, false, fields: _*)
  def apply(name: String, parent: => ValueType, isAbstract: Boolean, fields: Field*): ValueType = 
      new ValueType(name, parent, isAbstract, fields)
      
  def unapply(vt: ValueType): Option[(String, Map[Field.Name,Field])] = Some((vt.name, vt.fields))
}

sealed class ValueType(
    name: String, 
    parentType: => ValueType = AnyValueType, 
    override val isAbstract: Boolean,
    fieldSet: Seq[Field] = Seq()) // Call-by-value, since recursive field types are forbidden for value types.
  extends ObjectType(name, parentType, false, false, fieldSet) {
  
  type Value = ValueData

  def metaType: MetaAnyType = MetaValueType
  
  override lazy val parent: ValueType = parentType
  
  override def emptyData = ValueData(this)
  
  override def asValue(a: Any): Value = a match {
    case data: ValueData => 
      if ((data.resourceType != this) && (data.resourceType <:< this)) {
        data.resourceType asValue a
      }
      else {
        val fixed = for ((name, v) <- data.fields) 
                    yield name -> (fields(name).fieldType asValue v)
        ValueData(this, fixed)
      }
      
    case _ => throw new IllegalArgumentException("Expecting a ValueData object: " + a)
  }
  
  override def getFailures(value: Any): List[String] = {
    
    value match {
      case data: ValueData => 
        val wrongTypeErrors = if (data.resourceType <:< this) Nil
                              else List("Expected a " + name + " value, found: " + data.resourceType.name)
        val valueFields = data.resourceType.fields
        val fieldErrors = (List.empty[String] /: valueFields) { 
          case (errs, (name, field)) => 
            val fieldValue = data.fields.get(name)
            val fieldErrors = valueFields(name).getFailures(fieldValue).map(f => name + ": " + f)
            errs ++ fieldErrors
        }
        wrongTypeErrors ++ fieldErrors
        
      case x => List("Not a ValueData object: " + x + " of type " + x.getClass.getSimpleName)
    }
  }
}
