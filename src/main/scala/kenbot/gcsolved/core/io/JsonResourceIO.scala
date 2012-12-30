package kenbot.gcsolved.core.io

import kenbot.gcsolved.core.types.ResourceType
import java.io.DataOutput
import kenbot.gcsolved.core.ResourceLibrary
import kenbot.gcsolved.core.ResourceSchema
import java.io.DataInput
import kenbot.gcsolved.core.types.BoolType
import kenbot.gcsolved.core.types.DoubleType
import kenbot.gcsolved.core.types.IntType
import kenbot.gcsolved.core.types.StringType
import kenbot.gcsolved.core.types.ValueType
import sys.error
import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.core.types.AnyType
import kenbot.gcsolved.core.types.SelectOneType
import kenbot.gcsolved.core.types.FileType
import kenbot.gcsolved.core.types.ListType

object JsonResourceIO extends ResourceIO {

  def fileExtension: String = "json"

  def writeLibrary(lib: ResourceLibrary, out: DataOutput) {
    
  }

  def readLibrary(schema: ResourceSchema, in: DataInput): ResourceLibrary = ResourceLibrary("foo", schema)

  def write[A](resourceType: ResourceType, value: A, out: DataOutput): Unit = resourceType match {
    case IntType | StringType | DoubleType | BoolType =>
    /*case FileType(category, extensions) => writeLine(out, value.asInstanceOf[File].getName)
    case ListType(elementType, _) => writeList(elementType, value.asInstanceOf[List[Any]], out)
    case vt: ValueType => writeObjectData(value.asInstanceOf[ValueData], out)
    case rt: RefType => writeResourceRef(value.asInstanceOf[ResourceRef], out)
    case s1t: SelectOneType => writeLine(out, value)
    case AnyType => writeAnyData(value.asInstanceOf[AnyData], out)*/
    case x => error("Unknown type: " + x) 
  }
  
  def read(resourceType: ResourceType, lib: ResourceLibrary, in: DataInput): Any = ()

}