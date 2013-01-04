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
import scala.collection.mutable.StringBuilder
import kenbot.gcsolved.core.ObjectData
import kenbot.gcsolved.core.ResourceRef
import java.io.File
import kenbot.gcsolved.core.AnyData

object JsonResourceIO extends ResourceIO {//}with JsonResourceWriter {

  def fileExtension: String = "json"

  def writeLibrary(lib: ResourceLibrary, out: DataOutput) {}
  def write[A](resourceType: ResourceType, value: A, out: DataOutput) {}
  def readLibrary(schema: ResourceSchema, in: DataInput): ResourceLibrary = ResourceLibrary("foo", schema)
  def read(resourceType: ResourceType, lib: ResourceLibrary, in: DataInput): Any = ()
}

trait JsonResourceWriter extends ResourceWriterBase {
  
  def writeInt(value: Int, out: DataOutput): Unit = writeAtom(value.toString, out)
  def writeBool(value: Boolean, out: DataOutput): Unit = writeAtom(value.toString, out)
  def writeString(value: String, out: DataOutput): Unit = writeAtom("\"" + encode(value) + "\"", out)
  def writeDouble(value: Double, out: DataOutput): Unit = writeAtom(value, out)
  def writeFile(file: File, out: DataOutput): Unit = writeString(file.getName, out)
  def writeAnyData(anyData: AnyData, out: DataOutput): Unit = writeMap(Map(), out)
  def writeList(listType: ListType, list: List[Any], out: DataOutput)
  def writeObjectData(objectData: ObjectData, out: DataOutput)
  def writeResourceRef(ref: ResourceRef, out: DataOutput)
  def writeLibraryHeader(lib: ResourceLibrary, out: DataOutput)
  def writeLibraryFooter(lib: ResourceLibrary, out: DataOutput)
  
  private def writeMap(obj: Map[_, _], out: DataOutput) {
    writeLine(out, "{")
    for ((k,v) <- obj) {
      writeString(k.toString, out) 
      writeAtom(": " + v, out)
      writeLine(out)
    }
    writeLine(out, "}")
  }

  
  
  private def writeAtom(a: Any, out: DataOutput): Unit = out writeBytes a.toString
  private def encode(string: String): String = string flatMap {
    case '\"' => "\\\""
    case '\\' => "\\\\"
    case '/' => "\\/"
    case '\b' => "\\\b"
    case '\f' => "\\\f"
    case '\n' => "\\\n"
    case '\r' => "\\\r"
    case '\t' => "\\\t"
    case a => a.toString
  }
}

