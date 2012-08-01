package kenbot.gcsolved.resource.types
import org.scalatest._
import matchers._
import sys.error
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BoolTypeSpec extends Spec with ShouldMatchers {
  
  describe("Boolean") {
    it("should accept true/false") {
      BoolType acceptsValue (true) should be (true)
      BoolType acceptsValue (false) should be (true)
    }
    it("should accept values that look like boolean values") {
      BoolType acceptsValue ("True") should be (true)
      BoolType acceptsValue ("false") should be (true)
    }
    it("should not accept non-boolean values") {
      BoolType acceptsValue ("fred") should be (false)
    }
  }
}