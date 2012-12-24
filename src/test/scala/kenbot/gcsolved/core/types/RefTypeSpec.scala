package kenbot.gcsolved.core.types
import scala.sys.error
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._
import kenbot.gcsolved.core.Field
import Field._
import kenbot.gcsolved.core.ResourceRef
import kenbot.gcsolved.core.RefData
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class RefTypeSpec extends Spec with ShouldMatchers {

  def idField: Field = 'id -> StringType ^ (isId = true)
  
  describe("Reference types") {
    val fruit = RefType("fruit", idField, 'healthy -> BoolType)
    val hammer = RefType("hammer", idField)
    val banana = RefType("banana", fruit, false, 'peeled -> BoolType, 'latinName -> StringType ^ (default=Some("defaultus")))
    
    describe("construction") {
      it ("should set the right values") {
        banana.name should equal ("banana")
        banana.parent should equal (fruit)
        fruit.fields.keys should contain ("healthy")
        banana.fields.keys should contain ("peeled")
        banana.isAbstract should equal (false)
      }
      it("should be non-abstract by default") {
        fruit should not be ('isAbstract)
      }
    }
    
    describe("empty data") {
      it("should contain default values") {
        val empty = banana.emptyData
        empty("latinName") should equal ("defaultus")
        
      }
    }
    
    describe("equality") {
      it("should equal a type with the same properties") {
         banana should equal (RefType("banana", fruit, false, 'peeled -> BoolType, 'latinName -> StringType ^ (default=Some("defaultus"))))
      }
      it("shouldn't equal a type with different fields") {
         banana should not equal (RefType("banana", fruit, false, 'peeled -> IntType))
      }
      it("shouldn't equal a type with a different name") {
         banana should not equal (RefType("apple", fruit, false, 'peeled -> BoolType))
      }
      it("shouldn't equal a value type with a different supertype") {
         banana should not equal (RefType("apple", idField, 'peeled -> BoolType))
      }
      it("shouldn't equal a type with a different abstractness") {
         banana should not equal (RefType("apple", fruit, true, 'peeled -> BoolType))
      }
    }
    
    describe("inheritance") {
      describe("check") {
        it("should return false if they are unrelated") {
          hammer <:< fruit should be (false)
          fruit <:< banana should be (false)
        }
        
        it("should return true if they are related") {
          banana <:< fruit should be (true)
        }
      }
      
      it("should inherit fields from supertype") {
        banana.fields.keys should contain ("healthy")
      }
    }
    
    describe("validation") {
      describe("of AnyRefType") {
        it("should accept any resource reference") {
          val refType = RefType("grumpus", idField)
          AnyRefType acceptsValue ResourceRef("bumblefoo", refType) should be (true)
        }
        
        it("should not accept any other value") {
          val refType = RefType("grogan", idField)
          AnyRefType acceptsValue RefData(refType, "id" -> "abc") should be (false)
        }
      }

      it("should accept ResourceRefs of the named type") {
        banana acceptsValue (ResourceRef("1234", banana)) should be (true)
      }
      it("should accept ResourceRefs of a sub-type of the named type") {
        fruit acceptsValue (ResourceRef("1234", banana)) should be (true)
      }
      it("should not accept ResourceRefs of the wrong type") {
        fruit acceptsValue (ResourceRef("1234", RefType("froo", idField))) should be (false)
      }
      
      it("should not accept non-ResourceRefs") {
        fruit acceptsValue (List(1,2,3,4)) should be (false)
      }
    }
    
    describe("Recursive RefTypes") {
      it("shouldn't self destruct") {
        lazy val mirror: RefType = RefType.recursive("mirror", Seq(idField, 'mirror -> mirror))
        
        mirror.fields("mirror").fieldType should equal (mirror)
      }
    }
    
    describe("Fields") {
      it("should be in the same order it was declared in") {
        val someType = RefType("someType",
            'a -> StringType ^ (isId = true), 
            'b -> StringType, 
            'c -> StringType, 
            'd -> StringType)
        someType.fields.keys.toSeq should equal (Seq("a", "b", "c", "d"))
      }
      
      describe("with no ID") {
        it("should cause an error if the type is not abstract") {
          evaluating {  
            RefType("ConcreteNoID", 'a -> StringType).fields
          } should produce [IllegalArgumentException]
        }
        it("shouldn't cause an error if the type is abstract") {
          RefType("AbstractNoID", AnyRefType, true, 'a -> StringType).fields
        }
      }
      
      describe("with many IDs") {
        it("should cause an error") {
          evaluating {  
            RefType("ConcreteManyIDs", 
                'a -> StringType ^ (isId = true), 
                'b -> IntType ^ (isId = true)).fields
          } should produce [IllegalArgumentException]
        }
      }
    }
  }
  
}
  