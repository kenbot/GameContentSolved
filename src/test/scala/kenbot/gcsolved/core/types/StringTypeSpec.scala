package kenbot.gcsolved.core.types
import org.scalatest._
import sys.error
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class StringTypeSpec extends FunSpec with ShouldMatchers {
  
  describe("Accepting values") {
    it("should accept strings") {
      StringType acceptsValue ("Asdfasdf") should be (true)
    }
    it("should not accept non-strings") {
      BoolType acceptsValue (77777) should be (false)
    }
  }
  
  describe("Type conformance") {
    
    it("should conform with AnyType") {
      StringType <:< AnyType should be (true)
    }
    
    it("should not conform if the other type is not a String") {
      StringType <:< IntType should be (false)
    }
    
    it("should conform if both StringTypes are unbounded") {
      StringType(None) <:< StringType(None) should be (true)
    }

    it("should conform if we have a max length and the other type doesn't") {
      StringType(Some(4)) <:< StringType(None) should be (true)
    }
    
    it("should conform if the other type's max length is less strict (ie higher)") {
      StringType(Some(22)) <:< StringType(Some(99)) should be (true)
    }
    
    it("should not conform if the other type's max length is more strict (ie lower)") {
      StringType(Some(66)) <:< StringType(Some(11)) should be (false)
    }
    
    it("should not conform if the other type has a max length and we don't") {
      StringType(None) <:< StringType(Some(66)) should be (false)
    }
    
  }
}
