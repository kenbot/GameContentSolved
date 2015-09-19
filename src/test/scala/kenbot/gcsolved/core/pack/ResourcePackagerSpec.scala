package kenbot.gcsolved.core.pack

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import java.io.File

abstract class ResourcePackagerSpec(val packager: ResourcePackager) extends FunSpec with ShouldMatchers {
   
  val baseDir = new File("test-pack-env")

  describe("Packing") {
    it("should fail trying to pack something that doesn't exist") {
      evaluating { packager.pack(new File("asdfsdd12354"), baseDir) } should produce [IllegalArgumentException]
    }
        
    it("should fail trying to pack a non-directory") {
      givenSomeFile("foo.txt") { file => 
        evaluating { packager.pack(file, baseDir) } should produce [IllegalArgumentException]
      }
    }
    
    it("should result in a file that exists") {
      givenSomeDirectory { dir => 
        packager.pack(dir, baseDir) should be ('exists) 
      }
    }
    
    it("should result in a file that has the expected extension") {
      givenSomeDirectory { dir => 
        val expectedExt = DefaultResourcePackager.packageExtension
        packager.pack(dir, baseDir).getName endsWith expectedExt should be (true)
      }
    }
  }
  
  describe("Unpacking") {
    it("should fail trying to unpack a directory") {
      givenSomeDirectory { dir =>       
        evaluating { packager.unpack(dir, baseDir) } should produce [IllegalArgumentException]
      }
    }
    
    it("should fail trying to unpack a file that doesn't exist") {
      evaluating { packager.unpack(new File("asdfasdf"), baseDir) } should produce [IllegalArgumentException]
    }
  }
  
  def givenSomeFile(name: String)(thunk: File => Unit) {
    val file = new File("src/test/resources/" + name)
    require(file.createNewFile(), "Couldn't create a file: " + file)

    try thunk(file) 
    finally require(file.delete())
  }
  
  def givenSomeDirectory(thunk: File => Unit) {
    val someDirectory = new File("src/test/resources/test-dir")
    
    require(someDirectory.mkdirs, "Couldn't create a directory for packaging" + someDirectory.getPath)
    
    def deltree(f: File) { 
      if (f.isDirectory) f.listFiles foreach deltree
      require(f.delete) 
    }
    
    try thunk
    finally deltree(someDirectory)
  }
  
}
