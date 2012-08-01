package kenbot.gcsolved.resource.types
import kenbot.gcsolved.resource.meta._

object BoolType extends ResourceType("Bool") {
  type Value = Boolean

  def metaType: MetaAnyType = MetaBoolType
  
  def asValue(a: Any): Boolean  = a match {
    case b: Boolean => b
    case x => x.toString.toBoolean
  }
}