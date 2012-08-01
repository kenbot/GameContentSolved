package kenbot.gcsolved.resource.io

import java.io.{DataOutput, DataInput, File}
import scala.annotation.tailrec
import kenbot.gcsolved.resource.types.ResourceType
import kenbot.gcsolved.resource.ResourceSchema
import kenbot.gcsolved.resource.ResourceLibrary

trait ResourceIO {
  def fileExtension: String
  
  def writeLibrary(lib: ResourceLibrary, out: DataOutput): Unit
  def readLibrary(schema: ResourceSchema, in: DataInput): ResourceLibrary

  def write[A](resourceType: ResourceType, value: A, out: DataOutput): Unit
  def read(resourceType: ResourceType, lib: ResourceLibrary, in: DataInput): Any
}





