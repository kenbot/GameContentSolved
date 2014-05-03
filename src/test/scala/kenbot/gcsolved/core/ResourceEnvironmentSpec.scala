package kenbot.gcsolved.core

import kenbot.gcsolved.core.io.DefaultResourceIO
import kenbot.gcsolved.core.pack.DefaultResourcePackager
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import kenbot.gcsolved.core.Field.symbolAndType2Field
import kenbot.gcsolved.core.Field.symbolAndValue2namePair
import kenbot.gcsolved.core.types.IntType
import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.core.types.StringType
import org.scalatest.junit.JUnitRunner
import kenbot.gcsolved.editor.Main
import herezod.HerezodSchema

@RunWith(classOf[JUnitRunner])
class ResourceEnvironmentSpec extends Spec with ShouldMatchers {
  
  val refType = RefType("Rabbit") defines ( 
      'numEars -> IntType, 
      'fluffiness -> IntType, 
      'name -> StringType ^ (isId = true))
  
  val io = DefaultResourceIO
  val packager = DefaultResourcePackager
  val schema = ResourceSchema().addRefTypes(refType)
  def packageExt = packager.packageExtension
  def ioExt = io.fileExtension
/*
  describe("Libraries in the environment") {
    it("should include all files with the right package extension") {
      givenAnEnvironment { env => 
        givenFiles(env, "moo.cow", "blah.txt") { _ => 
          givenPackageFiles(env, "foo", "bar", "banana") { 
            env.libraries.toSet should equal (Set("foo", "bar", "banana"))
          }
        }
      }
    }
  }
  
  describe("Unpackaged area") {
    it("should be empty in a new environment") {
      givenAnEnvironment { env => 
        env.unpackedArea should not be ('exists)
      }
    } 
    
    it("should exist after a save") {
      givenAnEnvironment { env => 
        val lib = ResourceLibrary("apple", schema)
        env.saveLibrary(lib)
        env.unpackedArea should be ('exists)
        
      }
    } 
    
    it("should be cleared by clearUnpackedArea()") {
      givenAnEnvironment { env => 
        val lib = ResourceLibrary("moo", schema)
        env.saveLibrary(lib)
        env.clearUnpackedArea()
        env.unpackedArea.listFiles.toList should be ('empty)
      }
    }
  }
  
  
  describe("Loading") {
    def createAndSaveLib(env: ResourceEnvironment, libName: String) = {
      val lib = ResourceLibrary(libName, "My test library", schema).addResources(
            RefData(refType, 'numEars -> 3, 'fluffiness -> 2, 'name -> "Bob"),
            RefData(refType, 'numEars -> 4, 'fluffiness -> 77, 'name -> "Fred"),
            RefData(refType, 'numEars -> 7, 'fluffiness -> 9, 'name -> "Jimbo"))
      env.saveLibrary(lib)
      env.clearUnpackedArea()
      lib
    }
    
    it("should unpack the library") {
      givenAnEnvironment { env =>
        val lib = createAndSaveLib(env, "gumby")
        env.loadLibrary(lib.ref)
        env.isUnpacked(lib.ref) should be (true)
      }
    }
    
    it("should return a library identical to the saved one") {
      givenAnEnvironment { env => 
        val lib = createAndSaveLib(env, "mcgee")
        val reloadedLib = env.loadLibrary(lib.ref)
        reloadedLib should equal (lib)
      }
    }
  }
  
  */
  
  describe("Saving") {
    
    def createLib(): ResourceLibrary = ResourceLibrary("mtl", "My test library", schema).addResources(
            RefData(refType, 'numEars -> 3, 'fluffiness -> 2, 'name -> "Sally"),
            RefData(refType, 'numEars -> 4, 'fluffiness -> 77, 'name -> "Jane"),
            RefData(refType, 'numEars -> 7, 'fluffiness -> 9, 'name -> "Carol"))
    
    /*
    it("should create a save file") {
      givenAnEnvironment { env => 
        val lib = createLib()
        env.saveLibrary(lib)
        val saveFileOpt = env.findPackedFile(lib.ref)
        saveFileOpt should be ('defined)
        saveFileOpt.get should be ('exists)
      }
    }
    
    it("should overwrite a currently open unpackaged dir") {
      pending
    }
    
    it("should save the resources") {
      givenAnEnvironment { env => 
        val lib = createLib()
        env.saveLibrary(lib)
        val dir = env.findUnpackedDirectory(lib.ref).get
        val resourceFile = new File(dir.getPath + "/resources." + ioExt)
        resourceFile should be ('exists)
      }
    }
    
    it("should save the schema") {
      givenAnEnvironment { env => 
        val lib = createLib()
        env.saveLibrary(lib)
        val dir = env.findUnpackedDirectory(lib.ref).get
        val schemaFile = new File(dir.getPath + "/schema." + ioExt)
        schemaFile should be ('exists)
      }
    }*/
    
  }
      
  describe("a HerezodSchema library") {
    it("should save and load correctly") {
      givenAnEnvironment { env => 
        val saved = ResourceLibrary("new", HerezodSchema.Schema)
        env saveLibrary saved
        val loaded = env loadLibrary saved.ref
        loaded should equal (saved)
      }
    }
  }
  
  private def givenPackageFiles(env: ResourceEnvironment, fileNames: String*)(thunk: => Unit) {
    fileNames foreach { f =>
      val lib = ResourceLibrary(f, schema)
      env.saveLibrary(lib) 
    }
    val files = fileNames.map(f => new File(env.homeDirectory + "/" + f + "." + packageExt))
    
    try thunk
    finally files.foreach(f => require(f.delete, "Couldn't delete file: " + f))
  }
  
  private def givenFiles(env: ResourceEnvironment, fileNames: String*)(thunk: Seq[File] => Unit) {
    val files = fileNames map { f => new File(env.homeDirectory + "/" + f) }
    
    files foreach { f => 
      require(f.createNewFile, "Couldn't create file: " + f.getPath) 
    }
    
    try thunk(files)
    finally files foreach { f => require(f.delete, "Couldn't delete file: " + f) }
  }
  
  private var nextEnvId = 1
  
  private def givenAnEnvironment(thunk: ResourceEnvironment => Unit) {
    val envName = "test-env" + nextEnvId
    nextEnvId += 1
    val homeDirectory = new File(envName)
    require(!homeDirectory.exists, "Home directory is already here; it might have been left by a previously failing test.")
    require(homeDirectory.mkdirs, "Couldn't create home directory " + homeDirectory.getPath)
    
    def deltree(f: File) { 
      if (f.isDirectory) f.listFiles foreach deltree
      val deleted = f.delete
      require(deleted, "Couldn't delete " + f.getPath + " in home directory cleanup") 
    }
    
    try thunk(ResourceEnvironment(homeDirectory, io, packager))
    finally deltree(homeDirectory)
  }
  
}
  