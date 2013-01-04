package kenbot.gcsolved.core.types
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import kenbot.gcsolved.core.Field.symbolAndType2Field
import kenbot.gcsolved.core.Field.symbolAndValue2namePair
import org.scalatest.junit.JUnitRunner
import kenbot.gcsolved.core.RefData
import kenbot.gcsolved.core.ValueData
import kenbot.gcsolved.core.Field

@RunWith(classOf[JUnitRunner])
class ValueTypeSpec extends Spec with ShouldMatchers {

  describe("Value types") {
    val fruit = ValueType("fruit") defines 'healthy -> BoolType
    val hammer = ValueType("hammer")
    val banana = ValueType("banana") extend fruit defines 'peeled -> BoolType
     
    describe("construction") {
      it ("should set the right values") {
        banana.name should equal ("banana")
        banana.parent should equal (fruit)
        fruit.fields.keys should contain ("healthy")
        banana.fields.keys should contain ("peeled")
      }
      
      it("should be non-abstract by default") {
        fruit should not be ('isAbstract)
      }
    }
    
    describe("equality") {
      it("should equal a type with the same properties") {
         banana should equal (ValueType("banana") extend fruit defines 'peeled -> BoolType)
      }
      it("should equal a nested value type with the same properties") {
        val RectType = ValueType("Rect") defines (
          'X1 -> IntType ^ (required=true), 
          'Y1 -> IntType ^ (required=true), 
          'X2 -> IntType ^ (required=true), 
          'Y2 -> IntType ^ (required=true))
          
        val ImageType = ValueType("Image").abstractly
        val StillImageType = ValueType("StillImage") extend ImageType defines 'Bounds -> RectType
        
        StillImageType should equal (StillImageType)
      }
      
      it("shouldn't equal a type with different fields") {
         banana should not equal (ValueType("banana") extend fruit defines 'peeled -> IntType)
      }
      it("shouldn't equal a type with a different name") {
         banana should not equal (ValueType("aple") extend fruit defines 'peeled -> BoolType)
      }
      it("shouldn't equal a value type with a different supertype") {
         banana should not equal (ValueType("apple") defines 'peeled -> BoolType)
      }
      it("shouldn't equal a type with a different abstractness") {
         banana should not equal (ValueType("apple").abstractly extend fruit defines 'peeled -> BoolType)
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
      val fruitData = ValueData(fruit, 'healthy -> false)
      val bananaData = ValueData(banana, 'healthy -> true, 'peeled -> false)

      describe("of AnyValueType") {
        it("should accept any value data") {
          AnyValueType acceptsValue ValueData(banana) should be (true)
        }
        
        it("should not accept any other value") {
          val refType = RefType("abc") defines ('id -> StringType ^ (isId = true))

          AnyValueType acceptsValue RefData(refType, 'id -> "grumpus") should be (false)
        }
      }
      
      it("should accept ValueData of the named type") {
        fruit acceptsValue fruitData should be (true)
      }
      it("should accept ValueData of a sub-type of the named type") {
        fruit acceptsValue bananaData should be (true)
      }
      it("should not accept ValueData of the wrong type") {
        fruit acceptsValue ValueData(hammer) should be (false)
      }
      
      it("should not accept non-ValueData") {
        fruit acceptsValue 1234 should be (false)
      }
    }
  }
}
  