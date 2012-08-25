package kenbot.gcsolved.resource

import kenbot.gcsolved.resource.types.ResourceType

import Field.Name
import scalaz.Scalaz.ValidationNEL
import scalaz._
import Scalaz._

sealed trait Field {self =>
  type Value
  type ValidationResult = ValidationNEL[String, Option[Value]]
  
  def name: Name
  
  lazy val humanReadableName: Name = {
    val str = new StringBuilder
    for (i <- 0 until name.length) {
      val ch = name(i)
      str.append(if (ch.isUpper) " " + ch else ch)
    }
    str.capitalize
  }
  
  def category: Name
  val fieldType: ResourceType {type Value = self.Value}
  def required: Boolean
  def isId: Boolean
  def default: Option[fieldType.Value]
  def description: String
  
  def ^(required: Boolean = false, 
        isId: Boolean = false, 
        default: Option[Any] = None,
        category: String = "",
        description: String = ""): Field = {
    
    Field(name, fieldType, category, required, isId, default map fieldType.asValue, description)
  }
  
  def getFailures(value: Option[Any]): List[String] = value match {
    case Some(a) => fieldType.getFailures(a)
    case None if required => List("This field is required.")
    case None => Nil
  }
  
  final def validate(value: Option[Any]): ValidationResult = getFailures(value) match {
    case Nil => Success(value map fieldType.asValue)
    case first :: rest => Failure(NonEmptyList(first, rest: _*))
  }
  
  
  def acceptsValue(value: Option[Any]): Boolean = value match {
    case Some(v) => fieldType acceptsValue v
    case None => !required
  }
  
  private def equality = (name, fieldType.toString, category, required, default, description)
  
  override def equals(a: Any) = a match {
    case f: Field => equality == f.equality
    case _ => false
  }
  override def hashCode() = equality.hashCode
  
  override def toString() = "Field(%s,%s)".format(name, fieldType)
}


object Field {
  
  type Name = String

  def apply[T](name: Name,
      fieldType: => ResourceType {type Value = T}, 
      category: Name = "",
      required: Boolean = false, 
      isId: Boolean = false,
      default: Option[T] = None,
      description: String = ""): Field  = {
    
    new FieldImpl[T](name, fieldType, category, required, isId, default, description)
  }
  
  def unapply(a: Any): Option[(ResourceType, Boolean, Option[Any])] = a match {
    case f: Field => Some(f.fieldType, f.required, f.default)
    case _ => None
  }
  
  private class FieldImpl[T](
      val name: Name, 
      fType: => ResourceType {type Value = T}, 
      val category: Name,
      requiredParam: Boolean, 
      val isId: Boolean,
      givenDefault: Option[T], 
      val description: String) extends Field { self => 
    
    require(name.trim.length > 0, "Name must not be empty")
    require(!isId || givenDefault.isEmpty, "ID field cannot have a default value")
    
    type Value = T
    
    lazy val default: Option[T] = if (isId) None 
                                  else givenDefault orElse fType.default
    
    
    val required: Boolean = requiredParam || isId
    
    
    lazy val fieldType: ResourceType {type Value = T} = { 
      fType ensuring (_ != null, "Field had null type: " + name) 
    }
  }

  implicit def symbolAndValue2namePair[A](nameAndValue: (Symbol, A)): (String, A) = {
    nameAndValue._1.name -> nameAndValue._2
  }

  implicit def symbolAndType2Field[T](nameAndType: (Symbol, ResourceType)): Field = {
    stringAndType2Field(nameAndType._1.name, nameAndType._2)
  }
  
  implicit def stringAndType2Field[T](nameAndType: (String, ResourceType)): Field  = {
    val (name, resourceType) = nameAndType
    Field[resourceType.Value](name, resourceType)
  }
    
}
