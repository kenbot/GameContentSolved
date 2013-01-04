package kenbot.gcsolved.core
package io

import java.io.DataInput
import java.io.DataOutput
import java.io.File

import scala.annotation.tailrec
import scala.sys.error

import DefaultResourceIO.Flags._
import kenbot.gcsolved.core.types.AnyRefType
import kenbot.gcsolved.core.types.AnyType
import kenbot.gcsolved.core.types.BoolType
import kenbot.gcsolved.core.types.DoubleType
import kenbot.gcsolved.core.types.FileType
import kenbot.gcsolved.core.types.IntType
import kenbot.gcsolved.core.types.ListType
import kenbot.gcsolved.core.types.ObjectType
import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.core.types.ResourceType
import kenbot.gcsolved.core.types.SelectOneType
import kenbot.gcsolved.core.types.StringType
import kenbot.gcsolved.core.types.ValueType
import kenbot.gcsolved.core.AnyData
import kenbot.gcsolved.core.ObjectData
import kenbot.gcsolved.core.RefData
import kenbot.gcsolved.core.ResourceLibrary
import kenbot.gcsolved.core.ResourceRef
import kenbot.gcsolved.core.ResourceSchema
import kenbot.gcsolved.core.ValueData



object DefaultResourceIO extends ResourceIO with DefaultResourceReader with DefaultResourceWriter {
  private[core] object Flags {
    val StartLibrary = "[start-library]"
    val EndLibrary = "[end-library]"
    val StartList = "[start-list]"
    val EndList = "[end-list]"
    val StartObject = "[start-object]"
    val EndObject = "[end-object]"
  }
  
  val fileExtension = "library"
}


trait DefaultResourceWriter extends ResourceWriterBase {
  
  def writeLibraryHeader(lib: ResourceLibrary, out: DataOutput) {
    writeLine(out, StartLibrary)
    writeLine(out, lib.id)
    writeLine(out, lib.name)
  }
  
  def writeLibraryFooter(lib: ResourceLibrary, out: DataOutput) {
    writeLine(out, EndLibrary)
  }
  
  def writeInt(value: Int, out: DataOutput): Unit = writeLine(out, value)
  def writeBool(value: Boolean, out: DataOutput): Unit = writeLine(out, value)
  def writeString(value: String, out: DataOutput): Unit = writeLine(out, value)
  def writeDouble(value: Double, out: DataOutput): Unit = writeLine(out, value)
  def writeFile(file: File, out: DataOutput): Unit = writeLine(out, file.getName)

  def writeResourceRef(ref: ResourceRef, out: DataOutput) {
    writeLine(out, ref.refType.name)
    writeLine(out, ref.id)
  }
  
  def writeAnyData(anyData: AnyData, out: DataOutput) {
    import meta._
    write(MetaValueType, anyData.resourceType.typeDescriptor, out)
    write(anyData.resourceType, anyData.value, out)
  }
  
  def writeList(listType: ListType, list: List[Any], out: DataOutput) {
    writeLine(out, StartList)
    list.foreach(write(listType.elementType, _, out))
    writeLine(out, EndList)
  }

  def writeObjectData(objectData: ObjectData, out: DataOutput) {
    val objectType = objectData.resourceType
    
    writeLine(out, StartObject)
    writeLine(out, objectType.name)
    
    objectData.fields foreach { 
      case (fieldName, v) => 
        writeLine(out, fieldName)
        val field = objectType.fields(fieldName)
        write(field.fieldType, v, out)
    }
    writeLine(out, EndObject)
  }

}


trait DefaultResourceReader {

  private var lineNumber = 0
  
  def expectFlag(in: DataInput, flag: String) {
    val line = readLine(in)
    require(line exists (flag==), "Expected flag " + flag + ", found " + line + " at line " + lineNumber)
  }
  
  def readLine(in: DataInput): Option[String] = {
    lineNumber += 1
    Option(in.readLine).map(_.trim).filterNot(_ startsWith "[end")
  }
  
  def readLineOrFail(in: DataInput, errorMsg: => String): String = readLine(in) getOrElse error(errorMsg)
  
  def readLibrary(schema: ResourceSchema, in: DataInput): ResourceLibrary = {
    expectFlag(in, StartLibrary)
    lineNumber = 0
    
    @tailrec 
    def readResourceLoop(currentLib: ResourceLibrary): ResourceLibrary = {
      readObjectData(AnyRefType, currentLib, in) match {
        case Some(data: RefData) => readResourceLoop(currentLib addResource data)
        case _ => currentLib
      }
    }
    
    val id = readLineOrFail(in, "Couldn't read library id")
    val name = readLineOrFail(in, "Couldn't read library name")
    println("library id: " + id)
    readResourceLoop(ResourceLibrary(id, name, schema))
  }

  def read(resourceType: ResourceType, lib: ResourceLibrary, in: DataInput): resourceType.Value = {
    readOpt(resourceType, lib, in) getOrElse error("Expecting " + resourceType.name + "; nothing found")
  }
  
  def readOpt(resourceType: ResourceType, lib: ResourceLibrary, in: DataInput): Option[resourceType.Value] = {
    def forLine[A](f: String => A) = readLine(in).map(str => resourceType asValue f(str))
    resourceType match {
      case IntType(_,_) => forLine(_.toInt)
      case DoubleType => forLine(_.toDouble)
      case BoolType => forLine(_.toBoolean) 
      case StringType => forLine(identity)
      case FileType(_,_) => forLine(str => new File(str))
      case ListType(elementType, _) => readList(elementType, lib, in).map(resourceType asValue _)
      case rt: RefType => readResourceRef(lib.schema, in).map(resourceType asValue _)
      case vt: ValueType => readObjectData(vt, lib, in).map(resourceType asValue _)
      case s1t: SelectOneType => 
        for (v <- readOpt(s1t.valueType, lib, in) if s1t.values contains v) 
        yield resourceType asValue v
      case AnyType => readAnyData(in, lib).map(resourceType asValue _)
      case x => sys.error("Unknown type: " + x) 
    }
  }
  
  def readResourceRef(schema: ResourceSchema, in: DataInput): Option[ResourceRef] = {
    for {
      refTypeName <- readLine(in)
      refType <- schema.findRefType(refTypeName)
      id <- readLine(in)
    } 
    yield ResourceRef(id, refType)
  }
  
  def readAnyData(in: DataInput, lib: ResourceLibrary): Option[AnyData] = {
    import meta._
    val context = new SchemaContext(lib.schema)
    import context._
    
    for {
      resourceTypeData: ValueData <- readOpt(MetaAnyType, lib.schema.asLibrary, in)
      resourceType = resourceTypeData.asResourceType
      value <- readOpt(resourceType, lib, in)
    } 
    yield AnyData(value, resourceType)
  }
  
  def readObjectData(objectType: ObjectType, lib: ResourceLibrary, in: DataInput): Option[ObjectData] = {
    @tailrec
    def readFieldLoop(currentData: ObjectData): ObjectData = {
      val nextData = for {
        fieldName <- readOpt(StringType, lib, in)
        fieldType = currentData.resourceType.getFieldType(fieldName)
        fieldValue <- readOpt(fieldType, lib, in)
      } 
      yield currentData.updateField(fieldName, fieldValue)
      
      nextData match { 
        case Some(d) => readFieldLoop(d)
        case None => currentData
      }
    }
    
    if (readLine(in) != Some(StartObject)) return None
    
    for {
      actualTypeName <- readLine(in)
      actualType <- lib.schema.findObjectType(actualTypeName)
    } 
    yield readFieldLoop(actualType.emptyData)
  }

  def readList(elementType: ResourceType, lib: ResourceLibrary, in: DataInput): Option[List[elementType.Value]] = {
    
    if (readLine(in) != Some(StartList)) return None
    
    @tailrec
    def readNextItem(list: List[elementType.Value]): List[elementType.Value] = {
      readOpt(elementType, lib, in) match {
        case Some(e) => readNextItem(e :: list)
        case None => list
      }
    }
    Some(readNextItem(Nil).reverse)
  }
}
