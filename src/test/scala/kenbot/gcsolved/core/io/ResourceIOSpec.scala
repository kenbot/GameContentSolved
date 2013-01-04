package kenbot.gcsolved.core.io

import java.io.{DataInput, DataInputStream, DataOutput, DataOutputStream, File, FileInputStream, FileOutputStream}

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

import kenbot.gcsolved.core.{RefData, ResourceLibrary, ResourceRef, ResourceSchema, ValueData}
import kenbot.gcsolved.core.AnyData
import kenbot.gcsolved.core.Field.{symbolAndType2Field, symbolAndValue2namePair}
import kenbot.gcsolved.core.types.{AnyRefType, AnyType, AnyValueType, BoolType, DoubleType, FileType, IntType, ListType, RefType, SelectOneType, StringType, ValueType}


abstract class ResourceIOSpec(val resourceIO: ResourceIO) extends Spec with ShouldMatchers {
  
  val refType = RefType("Reffy") defines (
      'id -> StringType ^ (isId = true),
      'aaa -> StringType, 
      'bbb -> IntType)
      
  val valueType = ValueType("Vally") defines (
      'aaa -> StringType, 
      'bbb -> IntType)
      
  val nestedValueType = ValueType("Nesty") defines (
      'ccc -> StringType, 
      'fumble -> valueType)
      
  val mapKeyType = SelectOneType("enum", StringType, "a", "b", "c")
      
  val complicatedRefType: RefType = RefType("Complicaty") definesLazy Seq(
      'id -> StringType ^ (isId = true),
      'xxx -> StringType, 
      'yyy -> ListType(nestedValueType), 
      'self -> complicatedRefType)
      
  complicatedRefType.fields("self").fieldType.toString
      
  val schema = ResourceSchema().addValueTypes(valueType, nestedValueType).addRefTypes(refType, complicatedRefType)
  val lib = ResourceLibrary("TestLib", schema)
  
  val outputFile = "src/test/resources/out.txt"

  
  describe("ResourceIO") {
    it("should be able to write and read back an integer") {
      withDataStream { (in, out) =>
        resourceIO.write(IntType, 5677, out)
        resourceIO.read(IntType, lib, in) should equal (5677)
      }
    }
    it("should be able to write and read back a double") {
      withDataStream { (in, out) =>
        resourceIO.write(DoubleType, 3.6788, out)
        resourceIO.read(DoubleType, lib, in).asInstanceOf[Double] should be (3.6788 plusOrMinus 0.001)
      }
    }
    it("should be able to write and read back a boolean") {
      withDataStream { (in, out) =>
        resourceIO.write(BoolType, false, out)
        resourceIO.read(BoolType, lib, in) should equal (false)
      }
    }
    it("should be able to write and read back a string") {
      withDataStream { (in, out) =>
        resourceIO.write(StringType, "Little Miss Muffet", out)
        resourceIO.read(StringType, lib, in) should equal ("Little Miss Muffet")
      }
    }

    it("should be able to write and read back a select-one value") {
      val selectOneType = SelectOneType("Foo", StringType, "a", "b", "c")
      
      withDataStream { (in, out) =>
        resourceIO.write(selectOneType, "b", out)
        resourceIO.read(selectOneType, lib, in) should equal ("b")
      }
    }
    
    it("should be able to write and read back a filename") {
      val fileType = new FileType("text", "*.txt")
      
      withDataStream { (in, out) =>
        resourceIO.write(fileType, new File("abc.text"), out)
        resourceIO.read(fileType, lib, in) should equal (new File("abc.text"))
      }
    }
     
    it("should be able to write and read back resource reference") {
      val ref = ResourceRef("foo", refType)
      
      withDataStream { (in, out) =>
        resourceIO.write(refType, ref, out)
        resourceIO.read(refType, lib, in) should equal (ref)
      }
    }
    
    it("should be able to write and read back a list") {
      val listType = ListType(IntType) 
      
      withDataStream { (in, out) =>
        resourceIO.write(listType, List(-1, 5, 77), out)
        resourceIO.read(listType, lib, in) should equal (List(-1, 5, 77))
      }
    }
    
    it("should be able to write and read a value typed as any type") {
      val listType = ListType(IntType) 
      
      withDataStream { (in, out) =>
        val anyValue = AnyData(List(-1, 5, 77), listType)
        resourceIO.write(AnyType, anyValue, out)
        resourceIO.read(AnyType, lib, in) should equal (anyValue)
      } 
    }
    
    it("should be able to write and read back a resource reference typed as any resource reference") {
      val ref = ResourceRef("foo", refType)
      
      withDataStream { (in, out) =>
        resourceIO.write(AnyRefType, ref, out)
        resourceIO.read(AnyRefType, lib, in) should equal (ref)
      }
    }
    
    it("should be able to write and read value-data typed as any value-data") {
      val valueData = ValueData(valueType, 'aaa -> "blahblah", 'bbb -> 66)
      
      withDataStream { (in, out) =>
        resourceIO.write(AnyValueType, valueData, out)
        resourceIO.read(AnyValueType, lib, in) should equal (valueData)
      }
    }

    it("should be able to write and read back a list of lists") {
      val listListType = ListType(ListType(IntType))
      val listOfLists = List(List(-1, -2, -3, -4), List(5, 6, 7, 8), List(22,33,44,55))
      
      withDataStream { (in, out) =>
        resourceIO.write(listListType, listOfLists, out)
        resourceIO.read(listListType, lib, in) should equal (listOfLists)
      }
    }
    
    it("should be able to write and read back value data") {
      val valueData = ValueData(valueType, 'aaa -> "blahblah", 'bbb -> 66)
      
      withDataStream { (in, out) =>
        resourceIO.write(valueType, valueData, out)
        resourceIO.read(valueType, lib, in) should equal (valueData)
      }
    }
    
    it("should be able to write and read back nested value data") {
      val valueData = ValueData(nestedValueType, 
          'ccc -> "booboo", 
          'fumble -> ValueData(valueType, 
              'aaa -> "blahblah", 
              'bbb -> 66))
              
      withDataStream { (in, out) =>
        resourceIO.write(nestedValueType, valueData, out)
        resourceIO.read(nestedValueType, lib, in) should equal (valueData)
      }
    }
    
    it("should be able to write and read back a complicated library") {

      val valueData1 = ValueData(nestedValueType, 
          'ccc -> "booboo", 
          'fumble -> ValueData(valueType, 
              'aaa -> "blahblah", 
              'bbb -> 66))
              
      val valueData2 = ValueData(nestedValueType, 
          'ccc -> "dodo", 
          'fumble -> ValueData(valueType, 
              'aaa -> "happy", 
              'bbb -> 2))
              
      val complicatedResource1 = RefData(complicatedRefType, 
          'id -> "compy1",
          'xxx -> "super")
              
      val complicatedResource2 = RefData(complicatedRefType, 
          'id -> "compy2",
          'xxx -> "Foo",
          'yyy -> List(valueData1, valueData2),
          'self -> complicatedResource1.ref)

      val beforeLib = lib.addResources(complicatedResource1, complicatedResource2)
      
      withDataStream { (in, out) =>
        resourceIO.writeLibrary(beforeLib, out)
        val afterLib = resourceIO.readLibrary(schema, in)
        afterLib.allResources.toList should equal (beforeLib.allResources.toList)
      }
    }
  }
  

  private def withDataStream(thunk: (DataInput, DataOutput) => Unit) {
    val file = new File(outputFile)
    file.createNewFile
    def in = new DataInputStream(new FileInputStream(file))
    def out = new DataOutputStream(new FileOutputStream(file, false))
    try {
      thunk(in, out) 
    }
    finally {
      in.close()
      out.flush()
      out.close()
    }
  }
}
  