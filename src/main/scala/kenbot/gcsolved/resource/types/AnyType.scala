package kenbot.gcsolved.resource.types
import kenbot.gcsolved.resource.meta.MetaAnyType
import kenbot.gcsolved.resource.AnyData

object AnyType extends ResourceType("Any") {
  type Value = AnyData[Any]
  override def isAbstract = true
  override def getFailures(value: Any) = Nil
  def metaType: MetaAnyType = MetaAnyType
  def asValue(a: Any) = a.asInstanceOf[AnyData[Any]]
}
