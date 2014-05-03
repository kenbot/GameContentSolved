package kenbot.gcsolved.core.types

import scala.annotation.migration

import kenbot.gcsolved.core.Field
import kenbot.gcsolved.core.ValueData
import kenbot.gcsolved.core.meta._

object AnyValueType extends ValueType("AnyValue", isAbstract=true)  {
  
  override def fields: Map[Field.Name, Field] = Map.empty
  
  override def getFailures(value: Any): List[String] = {
    if (value.isInstanceOf[ValueData]) Nil
    else List("Expecting a ResourceRef: " + value)
  }
}
  
object ValueType {

  def of(fieldNames: SelectOneType, fieldType: ResourceType): ValueType = {
    val fields = fieldNames.values.map(n => Field[fieldType.Value](n.toString, fieldType))
    apply("") defines (fields: _*)
  }
  
  def apply(name: String) = new ValueType(name)
  def unapply(vt: ValueType): Option[(String, Map[Field.Name,Field])] = Some((vt.name, vt.fields))
}

sealed class ValueType(
    name: String, 
    parentType: => ValueType = AnyValueType, 
    isAbstract: Boolean = false,
    fieldSet: Seq[Field] = Seq()) // Call-by-value, since recursive field types are forbidden for value types.
  extends ObjectType(name, parentType, false, isAbstract, fieldSet) {
  
  override type Value = ValueData
  override type MyData = ValueData
  protected override type MyType = ValueType

  def metaType: MetaAnyType = MetaValueType
  
  
  override lazy val parent: ValueType = parentType
  
  override def emptyData = ValueData(this)
  
  override def apply(fields: (Field.Name, Any)*): ValueData = ValueData(this, fields: _*)
  
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
  
  def abstractly = new ValueType(name, parent, true, fieldSet)
  def extend(parent: => ValueType) = new ValueType(name, parent, isAbstract, fieldSet)
  def defines(fields: Field*) = new ValueType(name, parentType, isAbstract, fieldSet ++ fields)
  def definesLazy(fields: => Seq[Field]) = defines(fields: _*)
  
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
