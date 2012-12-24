package kenbot.gcsolved.core.types
import org.scalatest._
import sys.error
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

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