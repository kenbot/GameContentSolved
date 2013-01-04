package kenbot.gcsolved.core.io

import java.io.{DataOutput, File}

import scala.sys.error

import kenbot.gcsolved.core.{AnyData, ObjectData, ResourceLibrary, ResourceRef, ValueData}
import kenbot.gcsolved.core.types.{AnyType, BoolType, DoubleType, FileType, IntType, ListType, RefType, ResourceType, SelectOneType, StringType, ValueType}


trait ResourceWriterBase {
  def writeLibrary(lib: ResourceLibrary, out: DataOutput) {
    writeLibraryHeader(lib, out)
    lib.allResources foreach (writeObjectData(_, out))
    writeLibraryFooter(lib, out)
  }
  
  def write[A](rt: ResourceType, value: A, out: DataOutput): Unit = rt match {    
    case IntType => writeInt(IntType asValue value, out)
    case DoubleType => writeDouble(DoubleType asValue value, out)
    case BoolType => writeBool(BoolType asValue value, out)
    case StringType => writeString(StringType asValue value, out)
    case t: FileType => writeFile(t asValue value, out)
    case t: ListType => writeList(t, t asValue value, out)
    case t: ValueType => writeObjectData(value.asInstanceOf[ValueData], out)
    case t: RefType => writeResourceRef(t asValue value, out)
    case t: SelectOneType => writeSelectOneType(t, t asValue value, out)
    case AnyType => writeAnyData(AnyType asValue value, out)
    case x => error("Unknown type: " + x) 
  }
  
  def writeLine(out: DataOutput, str: Any = "") {
    out writeBytes str.toString
    out writeBytes "\n"
  }
  
  def writeSelectOneType(s1t: SelectOneType, value: Any, out: DataOutput): Unit = write(s1t.valueType, value, out)
  
  def writeInt(value: Int, out: DataOutput)
  def writeBool(value: Boolean, out: DataOutput)
  def writeString(value: String, out: DataOutput)
  def writeDouble(value: Double, out: DataOutput)
  def writeAnyData(anyData: AnyData, out: DataOutput)
  //def writeList(listType: ListType)(list: List[listType.Element], out: DataOutput) // 2.10
  //def writeSelectOneType(s1t: SelectOneType)(value: s1t.Value, out: DataOutput) // 2.10
  def writeFile(file: File, out: DataOutput)
  def writeList(listType: ListType, list: List[Any], out: DataOutput)
  def writeObjectData(objectData: ObjectData, out: DataOutput)
  def writeResourceRef(ref: ResourceRef, out: DataOutput)
  def writeLibraryHeader(lib: ResourceLibrary, out: DataOutput)
  def writeLibraryFooter(lib: ResourceLibrary, out: DataOutput)
}
