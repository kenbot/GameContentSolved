package kenbot.gcsolved.resource.types

trait UserType {
  this: ResourceType => 
  def name: String
}
