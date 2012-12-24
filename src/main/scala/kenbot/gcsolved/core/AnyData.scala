package kenbot.gcsolved.core
import kenbot.gcsolved.core.types.ResourceType

case class AnyData[+A](value: A, resourceType: ResourceType)