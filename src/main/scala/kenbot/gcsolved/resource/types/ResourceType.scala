package kenbot.gcsolved.resource.types

//import scalaz.Scalaz.ValidationNEL
//import scalaz._
//import Scalaz._
import kenbot.gcsolved.resource.meta.MetaAnyType


abstract class ResourceType protected[resource] (val name: String, parentType: => ResourceType = AnyType) {
  lazy val parent: ResourceType = parentType
  
  def <:<(other: ResourceType): Boolean =  
    (this != AnyType || other == AnyType) && (
      this == other || 
      other == AnyType || 
      (parent != this && (parent <:< other)))
      
      
  //type ValidationResult = ValidationNEL[String, Any]
  type Value    
  def isAbstract = false
  
  def metaType: MetaAnyType
  
  def default: Option[Value] = None
  
  /**
   * Convert or coerce the given value to the type expected by this ResourceType. 
   * While correctly typed, the returned value may or may not pass validation.
   * Throws an error if the conversion fails.
   */
  def asValue(a: Any): Value
  
  /**
   * Return a list of validation failures incurred by the given value.
   */
  def getFailures(value: Any): List[String] = defaultFailures(value)
  
  /**
   * Returns a default validation failure message if the given value
   * cannot be coerced to the expected type.
   */
  protected final def defaultFailures(value: Any): List[String] = {
    try { asValue(value); Nil } 
    catch { 
      case _ => List("Not a valid " + name + ": " + value) 
    }
  }
  
  /**
   * Validates the given value, returning a Scalaz Validation.
   *
  final def validate(value: Any): ValidationResult = getFailures(value) match {
    case Nil => Success(value)
    case first :: rest => Failure(NonEmptyList(first, rest: _*))
  }*/
  
  /**
   * Returns true if the given value is valid, false otherwise
   */
  final def acceptsValue(value: Any): Boolean = getFailures(value).isEmpty
  override def toString() = name
}