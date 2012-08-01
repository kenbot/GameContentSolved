package kenbot.gcsolved.resource
import kenbot.gcsolved.resource.types.ResourceType

case class AnyData[+A](value: A, resourceType: ResourceType)