package kenbot.gcsolved.core.types
import org.scalatest._
import sys.error
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class FileTypeSpec extends FunSpec with ShouldMatchers {
  
  describe("Accepting values") {
    val textFiles = FileType("text", "txt", "foo", "bar")
    it("should accept Files with an appropriate extension") {
      textFiles acceptsValue (new File("foo.txt")) should be (true)
    }
    it("should not accept files with a different extension") {

      textFiles acceptsValue (new File("foo.blob")) should be (false)
    }
    it("should not accept non-files") {
      textFiles acceptsValue ("fred") should be (false)
    }
  }
  
  describe("Type conformance") {
    it("should conform to AnyType") {
      FileType("foo") <:< AnyType should be (true)
    }  
    
    it("shouldn't conform to a different type") {
      FileType("foo") <:< StringType should be (false)
    }  
    
    it("should conform to a FileType with a less specific directory") {
      FileType("foo/bar") <:< FileType("foo") should be (true)
    }  
    
    it("shouldn't conform to a FileType with a more specific directory") {
      FileType("foo") <:< FileType("foo/bar") should be (false)
    }

    it("should conform to a FileType with less specific extensions") {
      FileType("foo", "x.a", "x.b", "x.c") <:< FileType("foo", "a", "b", "c") should be (true)
    }  
    
    it("shouldn't conform to a FileType with more specific extensions") {
      FileType("foo", "a", "b", "c") <:< FileType("foo", "a", "b", "x.c") should be (false)
    }  
  }
}
