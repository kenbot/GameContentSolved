package kenbot.gcsolved.core.types
import kenbot.gcsolved.core.meta.{ MetaAnyType, MetaNothingType }
import kenbot.gcsolved.core.AnyData

object NothingType extends ResourceType("Nothing") {
  type Value = Nothing
  override def isAbstract = true
  override def getFailures(value: Any) = Nil
  def metaType: MetaAnyType = MetaNothingType
  def asValue(a: Any) = sys.error("Nothing expected here: " + a)
}
