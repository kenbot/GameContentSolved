package kenbot.gcsolved.core.types

trait UserType {
  this: ResourceType => 
  def name: String
}
