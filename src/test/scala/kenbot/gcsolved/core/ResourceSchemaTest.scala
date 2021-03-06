package kenbot.gcsolved.core

import org.scalatest._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import Field._
import kenbot.gcsolved.core.types.AnyRefType
import kenbot.gcsolved.core.types.AnyValueType
import kenbot.gcsolved.core.types.IntType
import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.core.types.SelectOneType
import kenbot.gcsolved.core.types.StringType
import kenbot.gcsolved.core.types.ValueType
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class ResourceSchemaSpec extends FunSpec with ShouldMatchers {
  
  describe("Empty ResourceSchema") {
    it("should contain AnyRefType as the only RefType") {
      ResourceSchema.Empty.refTypes should equal (Seq(AnyRefType))
    }
    it("should contain AnyValueType as the only ValueType") {
      ResourceSchema.Empty.valueTypes should equal (Seq(AnyValueType))
    }
    it("should not contain any SelectOneTypes") {
      ResourceSchema.Empty.selectOneTypes should be ('empty)
    }
  }
  
  describe("User ref types") {
    val refType = RefType("foo") defines ('Name -> StringType ^ (isId=true), 'Age -> IntType)
    val schema = ResourceSchema().addRefTypes(refType)
    
    it("should contain the user type") {
      schema.userRefTypes.toList should contain (refType)
    }
    
    it("should contain only the user type") {
      schema.userRefTypes.length should be (1)
    }
  }

  describe("User value types") {
    val valueType = ValueType("foo") defines ('Name -> StringType, 'Age -> IntType)
    val schema = ResourceSchema().addValueTypes(valueType)
    
    it("should contain the user type") {
      schema.userValueTypes.toList should contain (valueType)
    }
    
    it("should contain only the user type") {
      schema.userValueTypes.length should be (1)
    }
  }
  
  describe("Selecting a non-existent") {
    val schema = ResourceSchema.Empty
    describe("RefType") {
      it("should return None") { schema.findRefType("@$#^H") should equal (None) }
    }
    describe("ValueType") {
      it("should return None") { schema.findValueType("GH&#7") should equal (None) }
    }
    describe("SelectOneType") {
      it("should return None") { schema.findSelectOneType("asd77h2H") should equal (None) }
    }
  }
  
  describe("Added RefTypes") {
    val refType = RefType("foo") defines ('Name -> StringType ^ (isId=true), 'Age -> IntType)
    val schema = ResourceSchema().addRefTypes(refType)
    
    it("should be in the list of RefTypes") {
      schema.refTypes should contain (refType)
    }
    
    it("should be accessible by name") {
      schema.findRefType("foo") should equal (Some(refType))
    }
    
    it("should be accessible by name as an object") {
      schema.findObjectType("foo") should equal (Some(refType))
    }
    
    describe("in linked schemas") {
      val schema2 = ResourceSchema().addLinkedSchema(schema)
      it("should be accessible by name") { 
        schema2.findRefType("foo") should equal (Some(refType))
      }
      it("should be accessible by name as an object") { 
        schema2.findObjectType("foo") should equal (Some(refType))
      }
    }
  }
  
  describe("Added ValueTypes") {
    val valueType = ValueType("foo") defines ('Name -> StringType, 'Age -> IntType)
    val schema = ResourceSchema().addValueTypes(valueType)
    
    it("should be in the list of ValueTypes") {
      schema.valueTypes should contain (valueType)
    }
    
    it("should be accessible by name") {
      schema.findValueType("foo") should equal (Some(valueType))
    }
    
    it("should be accessible by name as an object") {
      schema.findObjectType("foo") should equal (Some(valueType))
    }
    
    describe("in linked schemas") {
      val schema2 = ResourceSchema().addLinkedSchema(schema)
      it("should be accessible by name") { 
        schema2.findValueType("foo") should equal (Some(valueType))
      }
      it("should be accessible by name as an object") { 
        schema2.findObjectType("foo") should equal (Some(valueType))
      }
    }
  }
  
  describe("Added SelectOneTypes") {
    val selectOneType = SelectOneType("foo", StringType, "a", "b", "c")
    val schema = ResourceSchema().addSelectOneTypes(selectOneType)
    
    it("should be in the list of SelectOneTypes") {
      schema.selectOneTypes should contain (selectOneType)
    }
    
    it("should be accessible by name") {
      schema.findSelectOneType("foo") should equal (Some(selectOneType))
    }
    
    describe("in linked schemas") {
      val schema2 = ResourceSchema().addLinkedSchema(schema)
      it("should be accessible by name") { 
        schema2.findSelectOneType("foo") should equal (Some(selectOneType))
      }
    }
  }
}
