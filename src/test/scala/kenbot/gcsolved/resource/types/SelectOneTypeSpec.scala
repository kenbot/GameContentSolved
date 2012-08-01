package kenbot.gcsolved.resource.types
import org.scalatest._
import matchers._
import sys.error
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SelectOneTypeSpec extends Spec with ShouldMatchers {
  val selectOne = SelectOneType("FruitType", StringType, "Banana", "Apple", "Pear")
  
  describe("Value types") {
    describe("construction") {
      it ("should set the right values") {
        selectOne.name should equal ("FruitType")
        selectOne.values should equal (Seq("Banana", "Apple", "Pear"))
      }
    }
    describe("equality") {
      it("should equal a type with the same properties") {
         selectOne should equal (SelectOneType("FruitType", StringType, "Banana", "Apple", "Pear"))
      }
      it("shouldn't equal a type with different values") {
         selectOne should not equal (SelectOneType("FruitType", StringType, "Banana", "Apple", "Pear", "Durian"))
      }
      it("shouldn't equal a type with a different name") {
         selectOne should not equal (SelectOneType("FavouriteFruitType", StringType, "Banana", "Apple", "Pear"))
      }
    }
  }
  
  
  describe("validation") {
    it("should accept values that are in the value list") {
      selectOne acceptsValue "Banana" should be (true)
    }
    
    it("should not accept values that are not in the value list") {
      selectOne acceptsValue "blah" should be (false)
    }

  }
}
  