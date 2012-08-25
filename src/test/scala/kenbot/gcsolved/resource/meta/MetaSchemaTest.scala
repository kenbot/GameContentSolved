package kenbot.gcsolved.resource.meta

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import kenbot.gcsolved.resource.Field._
import kenbot.gcsolved.resource.types.AnyRefType
import kenbot.gcsolved.resource.types.AnyValueType
import kenbot.gcsolved.resource.types.BoolType
import kenbot.gcsolved.resource.types.IntType
import kenbot.gcsolved.resource.types.ListType
import kenbot.gcsolved.resource.types.RefType
import kenbot.gcsolved.resource.types.StringType
import kenbot.gcsolved.resource.types.ValueType
import org.scalatest.junit.JUnitRunner
import kenbot.gcsolved.resource.types.SelectOneType
import kenbot.gcsolved.resource.ValueData
import kenbot.gcsolved.resource.RefData
import kenbot.gcsolved.resource.Field
import kenbot.gcsolved.resource.ResourceSchema
import kenbot.gcsolved.resource.ResourceLibrary
import kenbot.gcsolved.resource.ResourceRef


@RunWith(classOf[JUnitRunner]) 
class MetaSchemaTest extends Spec with ShouldMatchers {
  
  def idField: Field = 'id -> StringType ^ (isId = true)
  
  describe("Fields") {
    val field = 'Boo -> IntType ^ (category="details", required=true, default=Some(5), description="hello!")
    val context = new SchemaContext(ResourceSchema.Empty)
    import context._
    
    def checkSameFields(data: ValueData, f: Field) {
      it("should have the same name") {
        data("Name") should equal (f.name)
      }
      it("should have the same field type") {
        data("FieldType") should equal (f.fieldType.typeDescriptor)
      }
      it("should have the same default") {
        data.get("Default") should equal (f.default)
      }
      it("should have the same required status") {
        data("Required") should equal (f.required)
      }
      it("should have the same description") {
        data("Description") should equal (f.description)
      }
      it("should have the same category") {
        data("Category") should equal (f.category)
      }
    }
    
    describe("when converted to data") {
      val boringField: Field = 'Meh -> StringType
      
      it("should have the MetaFieldType meta-type") {
        field.asData.resourceType should equal (MetaFieldType)
      }

      it("should be valid") {
        field.asData.valid should be (true)
      }
      
      it("shouldn't contain default if not set") {
        boringField.asData.fields contains "Default" should be (false)
      }
      
      it("shouldn't contain category if not set") {
        boringField.asData.fields contains "Category" should be (false)
      }
      
       it("shouldn't contain description if not set") {
        boringField.asData.fields contains "Description" should be (false)
      }
      
      checkSameFields(field.asData, field)
    }
    
    describe("when converted from data") {
      val data = ValueData(MetaFieldType, 
          'Name -> "Boo", 
          'FieldType -> IntType.typeDescriptor, 
          'Category -> "details", 
          'Required -> true, 
          'IsId -> false, 
          'Default -> 5, 
          'Description -> "hello!")
      
      val field2 = data.asField 
          
      checkSameFields(data, field2)
    }
    
    describe("when converting back and forward") {
      it ("should equal itself") {
        field.asData.asField should equal (field)
      }
    }
  }
  
  describe("RefTypes") {

    val parent = RefType("parent", AnyRefType, true, 'Blah -> StringType)
    val refType = RefType("foo", parent, false, idField, 'Flum -> IntType, 'Grum -> ListType(BoolType))
    val schema = ResourceSchema().addRefTypes(parent, refType)
    val context = new SchemaContext(schema)
    import context._   
    
    def checkSameFields(data: RefData, rt: RefType) {
      it("should have the same name") {
        data("Name") should equal (rt.name)
      }
      
      it("should have the same parent") {
        data("Parent") should equal (rt.parent.metaRef)
      }
      
      it("should have the same abstractness") {
        data("Abstract") should equal (rt.isAbstract)
      }
      
      it("should have the same fields") {
        data("Fields") should equal (rt.localFields.values.toList.map(_.asData))
      }
    }
    
    describe("when converted to data") {
      val data = refType.asData

      it("should have the RefTypeDefinition meta-type") {
        data.resourceType should equal (RefTypeDefinition)
      }

      it("should be valid") {
        data.valid should be (true)
      }
      
      checkSameFields(data, refType)
    }
    
    describe("when converted from data") {
      val data = RefData(RefTypeDefinition, 
          'Name -> "Boo", 
          'Parent -> parent.metaRef, 
          'Abstract -> false, 
          'Fields -> List[Field](
              'id -> StringType ^ (isId = true),
              'Mumbo -> IntType, 
              'Jumbo -> StringType).map(_.asData))
      
      val refType2 = data.asRefType
      
      checkSameFields(data, refType2)
    }
    
    describe("when converting back and forward") {
      it ("should equal itself") {
        refType.asData.asRefType should equal (refType)
      }
    }
  }
  
  describe("ValueTypes") {
    val parent = ValueType("parent", AnyValueType, true, 'Blah -> StringType)
    val valueType = ValueType("foo", parent, false, 'Flum -> IntType, 'Grum -> ListType(BoolType))
    val schema = ResourceSchema().addValueTypes(parent, valueType)
    val context = new SchemaContext(schema)
    import context._   
    
    def checkSameFields(data: RefData, vt: ValueType) {
      it("should have the same name") {
        data("Name") should equal (vt.name)
      }
      it("should have the same parent") {
        data("Parent") should equal (vt.parent.metaRef)
      }
      it("should have the same abstractness") {
        data("Abstract") should equal (vt.isAbstract)
      }
      it("should have the same fields") {
        data("Fields") should equal (vt.localFields.values.toList.map(_.asData))
      }
    }
    
    describe("when converted to data") {
      val data = valueType.asData
      
      it("should have the ValueTypeDefinition meta-type") {
        data.resourceType should equal (ValueTypeDefinition)
      }

      it("should be valid") {
        data.valid should be (true)
      }
      
      checkSameFields(data, valueType)
    }
    
    describe("when converted from data") {
      val data = RefData(ValueTypeDefinition, 
          'Name -> "Grum", 
          'Parent -> parent.metaRef, 
          'Abstract -> false, 
          'Fields -> List[Field](
              'Mumbo -> IntType, 
              'Jumbo -> StringType).map(_.asData))
      
      
      val valueType2 = data.asValueType

      checkSameFields(data, valueType2)
    }
    
    describe("when converting back and forward") {
      it ("should equal itself") {
        valueType.asData.asValueType should equal (valueType)
      }
    }
  }
  
  describe("SelectOneTypes") {
    val selectOneType = SelectOneType("foo", StringType, "a", "b", "c")
    val schema = ResourceSchema().addSelectOneTypes(selectOneType)
    val context = new SchemaContext(schema)
    import context._   
    
    def checkSameFields(data: RefData, s1t: SelectOneType) {
      it("should have the same name") {
        data("Name") should equal (s1t.name)
      }
      it("should have the same value type") {
        data("ValueType") should equal (s1t.valueType.typeDescriptor)
      }
      it("should have the same values") {
        data("Values") should equal (s1t.values)
      }
    }
    
    describe("when converted to data") {
      val data = selectOneType.asData
      
      it("should have the SelectOneTypeDefinition meta-type") {
        data.resourceType should equal (SelectOneTypeDefinition)
      }

      it("should be valid") {
        data.valid should be (true)
      }
      
      checkSameFields(data, selectOneType)
    }
    
    describe("when converted from data") {
      val data = RefData(SelectOneTypeDefinition, 
          'Name -> "Grum",
          'ValueType -> StringType.typeDescriptor,
          'Values -> List("a", "b", "c")
          
      )
      val selectOneType2 = data.asSelectOneType

      checkSameFields(data, selectOneType2)
    }
    
    describe("when converting back and forward") {
      it ("should equal itself") {
        selectOneType.asData.asSelectOneType should equal (selectOneType)
      }
    }
  }
  
  describe("ResourceSchema") {
    val fruitCompanyType = SelectOneType("FruitCompany", StringType, "Bob's Fruit Co", "BananaCorp", "Grocerinos")
    val seedType = ValueType("Seed", 'Color -> StringType, 'Size -> IntType)
    val fruitType = RefType("Fruit", AnyRefType, true, idField, 'Color -> StringType, 'Company -> fruitCompanyType)
    val bananaType = RefType("Banana", fruitType, false, 'Slipperiness -> IntType)
    val appleType = RefType("Apple", fruitType, false, 'Seeds -> ListType(seedType))
    
    
    val schema = ResourceSchema().
        addRefTypes(fruitType, bananaType, appleType).
        addValueTypes(seedType).
        addSelectOneTypes(fruitCompanyType)
        
    val context = new SchemaContext(schema)
    import context._   
        
    def checkSameFields(library: ResourceLibrary, schema: ResourceSchema) {
      
      it ("should have the same RefTypes") {
        val refTypes = library.allResourcesByType(RefTypeDefinition).map(_.asRefType).toSet
        refTypes should equal (schema.refTypes.toSet)
      }
      
      it ("should have the same ValueTypes") {
        val valueTypes = library.allResourcesByType(ValueTypeDefinition).map(_.asValueType).toSet
        valueTypes should equal (schema.valueTypes.toSet)
      }
      
      it ("should have the same SelectOneTypes") {
        val selectOneTypes = library.allResourcesByType(SelectOneTypeDefinition).map(_.asSelectOneType).toSet
        selectOneTypes should equal (schema.selectOneTypes.toSet)
      }
    }
        
    describe("when converted to a library") {
      val library = schema.asLibrary
      
      it("should be using the meta-schema") {
        library.schema should equal (MetaSchema)
      }

      it ("should be valid") {
        library should be ('valid)
      }
      checkSameFields(library, schema)
    }
    
    describe("when converted from a library") {
      val resources = List(AnyRefType.asData, AnyValueType.asData, fruitCompanyType.asData, seedType.asData, fruitType.asData, bananaType.asData, appleType.asData)
      val library: ResourceLibrary = SchemaLibrary.addResources(resources: _*)
      val schema2 = library.asSchema
      checkSameFields(library, schema2)
    }
    
    describe("when converting back and forward") {
      it ("should equal itself") {
        schema.asLibrary.asSchema should equal (schema)
      }
    }
  }
  
}