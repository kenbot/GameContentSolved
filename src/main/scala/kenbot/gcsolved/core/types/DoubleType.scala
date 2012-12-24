package kenbot.gcsolved.core.types
import kenbot.gcsolved.core.meta.MetaDoubleType
import kenbot.gcsolved.core.meta.MetaAnyType

object DoubleType extends ResourceType("Double") {
  type Value = Double
  
  def metaType: MetaAnyType = MetaDoubleType
  
  def asValue(a: Any): Double = a match {
    case d: Double => d
    case x => x.toString.toDouble
  }
}