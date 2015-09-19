package kenbot.gcsolved.core.types
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ListTypeSpec extends FunSpec with ShouldMatchers {
  
  describe("Accepting values") {
    val intList = ListType(IntType)
    
    it("should accept lists containing entirely appropriate values") {
      intList acceptsValue (List(1,2,3,4,-6)) should be (true)
    }
    
    it("should not accept lists containing some or all invalid values") {
      intList acceptsValue (List(1,2,3,"avv",-6)) should be (false)
    }
    
    it("should not accept non-lists") {
      intList acceptsValue ("foobar") should be (false)
    }
  }
  
  describe("Type conformance") {
    
    val A = StringType
    val AnimalType = ValueType("Animal")
    val PandaType = ValueType("Panda") extend AnimalType
    
    it("should conform with AnyType") {
      ListType(A) <:< AnyType should be (true)
    }
    
    it("should not conform if the other type is not a ListType") {
      ListType(A) <:< IntType should be (false)
    }
    
    it("should conform if both ListTypes have no max length") {
      ListType(A, None) <:< ListType(A, None) should be (true)
    }

    it("should conform if we have a max length and the other type doesn't") {
      ListType(A, Some(33)) <:< ListType(A, None) should be (true)
    }
    
    it("should conform if the other type's max length is less strict (ie higher)") {
      ListType(A, Some(22)) <:< ListType(A, Some(99)) should be (true)
    }
    
    it("should not conform if the other type's max length is more strict (ie lower)") {
      ListType(A, Some(66)) <:< ListType(A, Some(11)) should be (false)
    }
    
    it("should not conform if the other type has a max length and we don't") {
      ListType(A, None) <:< ListType(A, Some(66)) should be (false)
    }
    
    it("should conform if the element type conforms to the other's element type") {
      ListType(PandaType) <:< ListType(AnimalType) should be (true)
    }
    
    it("should not conform if the element type doesn't conform to the other's element type") {
      ListType(AnimalType) <:< ListType(PandaType) should be (false)
    }
  }
}
