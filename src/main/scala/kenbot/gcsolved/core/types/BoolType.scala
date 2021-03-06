package kenbot.gcsolved.core.types
import kenbot.gcsolved.core.meta._

object BoolType extends ResourceType("Bool") {
  type Value = Boolean

  def metaType: MetaAnyType = MetaBoolType
  override val default: Option[Boolean] = Some(false)
  
  def asValue(a: Any): Boolean  = a match {
    case b: Boolean => b
    case x => x.toString.toBoolean
  }
}