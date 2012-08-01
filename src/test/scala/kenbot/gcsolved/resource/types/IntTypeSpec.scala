package kenbot.gcsolved.resource.types
import org.scalatest._
import matchers._
import sys.error
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IntTypeSpec extends Spec with ShouldMatchers {
  
  describe("Accepting values") {
    it("should accept integers") {
      IntType acceptsValue (3) should be (true)
    }
    it("should accept things that look like integers") {
      IntType acceptsValue ("3") should be (true)
    }
    it("should not accept non-integers") {
      IntType acceptsValue (4.4) should be (false)
    }
  }
  
  describe("Type conformance") {
    
    it("should conform with AnyType") {
      IntType <:< AnyType should be (true)
    }
    
    it("should not conform if the other type is not an IntType") {
      IntType <:< StringType should be (false)
    }
    
    it("should conform if both IntTypes are unbounded") {
      IntType(None, None) <:< IntType(None, None) should be (true)
    }
    
    it("should conform if we have a lower bound and the other type doesn't") {
      IntType(Some(33), None) <:< IntType(None, None) should be (true)
    }
    
    it("should conform if the other type's lower bound is less strict (ie lower)") {
      IntType(Some(55), None) <:< IntType(Some(22), None) should be (true)
    }
    
    it("should not conform if the other type's lower bound is more strict (ie higher)") {
      IntType(Some(11), None) <:< IntType(Some(66), None) should be (false)
    }
    
    it("should not conform if the other type has a lower bound and we don't") {
      IntType(None, None) <:< IntType(Some(66), None) should be (false)
    }

    it("should conform if we have an upper bound and the other type doesn't") {
      IntType(None, Some(33)) <:< IntType(None, None) should be (true)
    }
    
    it("should conform if the other type's upper bound is less strict (ie higher)") {
      IntType(None, Some(22)) <:< IntType(None, Some(99)) should be (true)
    }
    
    it("should not conform if the other type's upper bound is more strict (ie lower)") {
      IntType(None, Some(66)) <:< IntType(None, Some(11)) should be (false)
    }
    
    it("should not conform if the other type has an upper bound and we don't") {
      IntType(None, None) <:< IntType(None, Some(66)) should be (false)
    }
  }
}