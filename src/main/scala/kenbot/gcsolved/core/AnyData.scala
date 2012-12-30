package kenbot.gcsolved.core
import kenbot.gcsolved.core.types.ResourceType

case class AnyData(rawValue: Any, resourceType: ResourceType) {
  def value: resourceType.Value = resourceType asValue rawValue
}