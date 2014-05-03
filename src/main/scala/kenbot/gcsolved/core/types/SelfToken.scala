package kenbot.gcsolved.core.types

import kenbot.gcsolved.core.AnyData
import kenbot.gcsolved.core.meta.MetaAnyType

/**
 * So that ResourceTypes can be easily self-referential, this SelfToken can be passed 
 * into ResourceType constructors, where it will be replaced by the actual ResourceType instance.
 * <p>
 * I'm not sure whether to support this yet or not; it might stem the bloodflow of infinite recursion bugs
 * when lazy values unwind at the wrong time.
 */
object SelfToken extends ResourceType("SELF") {
  private def error = sys.error("The SelfToken should only be passed in to ResourceType constructors.")
  
  override type Value = Nothing
  override def <:<(other: ResourceType): Boolean = error
  override def asAny(v: Value): AnyData = error
  override def isAbstract = error
  override def metaType: MetaAnyType = error
  override def default: Option[Value] = error
  override def asValue(a: Any): Value = error
  override def getFailures(value: Any): List[String] = error
}