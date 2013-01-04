package kenbot.gcsolved.core

import java.io._
import ResourceLibrary.ResourceLibraryRef
import kenbot.gcsolved.core.io.ResourceIO
import kenbot.gcsolved.core.pack.ResourcePackager
import kenbot.gcsolved.core.pack.DefaultResourcePackager
import kenbot.gcsolved.core.io.DefaultResourceIO
import scala.sys.error

sealed trait ResourceEnvironment {

  def resourcesFileName = "resources." + io.fileExtension
  def schemaFileName = "schema." + io.fileExtension
  
  def unpackedArea: File
  
  def packager: ResourcePackager
  def io: ResourceIO
  def homeDirectory: File
  def libraries: Seq[ResourceLibraryRef]

  def isLibraryFile(f: File): Boolean
  
  def isUnpacked(libraryRef: ResourceLibraryRef): Boolean = findUnpackedDirectory(libraryRef).isDefined
  def findPackedFile(libraryRef: ResourceLibraryRef): Option[File]
  def findUnpackedDirectory(libraryRef: ResourceLibraryRef): Option[File]
  def packLibrary(libraryRef: ResourceLibraryRef): File
  def unpackLibrary(libraryRef: ResourceLibraryRef): File
  def clearUnpackedArea(): Unit
  
  def saveLibrary(lib: ResourceLibrary): Unit
  def loadLibrary(libraryRef: ResourceLibraryRef): ResourceLibrary

}


object ResourceEnvironment {
  
  
  def apply(homeDirectoryFile: File, resourceIO: ResourceIO = DefaultResourceIO, resourcePackager: ResourcePackager = DefaultResourcePackager): ResourceEnvironment = new ResourceEnvironment {
    
    require(homeDirectoryFile.exists, "Home directory must exist: " + homeDirectoryFile)
    require(homeDirectoryFile.isDirectory, "Home directory must actually be a directory: " + homeDirectoryFile)
    
    val homeDirectory = homeDirectoryFile
    val io = resourceIO
    val packager = resourcePackager
    val expandedDirName = ".libs"
    
    lazy val libraries: Seq[ResourceLibraryRef] = {
      val libFiles = homeDirectory.listFiles.toList filter isLibraryFile
      libFiles map (f => getLibraryRefFromPackedFile(f.getName))
    }
    
    private def getLibraryRefFromPackedFile(packedFileName: String): String = {
      val extLength = packedFileName.length - packedFileName.lastIndexWhere('.' ==)
      packedFileName dropRight extLength
    }
    
    private def getPackedFileName(libraryRef: ResourceLibraryRef) = libraryRef + "." + packager.packageExtension
    
    def isLibraryFile(f: File): Boolean = f.isFile && f.getName.endsWith("." + packager.packageExtension)
    
    private def getUnpackedLibraryPath(libraryRef: ResourceLibraryRef) = unpackedArea.getAbsolutePath + "/" + libraryRef
    
    def unpackedArea: File = new File(homeDirectory.getPath + "/" + expandedDirName)
    
    def findUnpackedDirectory(libraryRef: ResourceLibraryRef): Option[File] = 
      unpackedArea.listFiles.find(f => f.isDirectory && f.getName == libraryRef)
    
    def findPackedFile(libraryRef: ResourceLibraryRef): Option[File] = 
      homeDirectory.listFiles.find(f => f.isFile && f.getName == getPackedFileName(libraryRef))

    
    def packLibrary(libraryRef: ResourceLibraryRef): File = {
      findUnpackedDirectory(libraryRef) match {
        case Some(unpackedDir) =>  packager.pack(unpackedDir, homeDirectory)
        case None => error("Cannot pack library '" + libraryRef + "', since it is not already unpacked")
      }
    }
    
    def unpackLibrary(libraryRef: ResourceLibraryRef): File = {
      require(unpackedArea.exists || unpackedArea.mkdir, "Could not create the unpacked area at \"" + unpackedArea.getPath + "\"")
      
      findPackedFile(libraryRef) match {
        case Some(packedFile) => packager.unpack(packedFile, unpackedArea)
        case None => error("Cannot unpack library '" + libraryRef + "', since the file does not exist")
      }
    }
    
    def clearUnpackedArea() = {
      def deltree(f: File) { 
        if (f.isDirectory) f.listFiles foreach deltree
        require(f.delete, "Couldn't delete file: " + f.getPath) 
      }
      unpackedArea.listFiles foreach deltree
    }
    
    def saveLibrary(lib: ResourceLibrary): Unit = {
      val saveDir = new File(getUnpackedLibraryPath(lib.ref))
      val resourcesFilename = saveDir + "/" + resourcesFileName
      val schemaFilename = saveDir + "/" + schemaFileName
      
      if (!saveDir.exists) {
        require(saveDir.mkdirs, "Couldn't create save directory '" + lib.ref + "'")
      }
      
      import meta._
      
      // TODO Capture files
      
      val schemaOut = new DataOutputStream(new FileOutputStream(schemaFilename, false))
      try io.writeLibrary(lib.schema.asLibrary, schemaOut) 
      finally schemaOut.close
      
      val resOut = new DataOutputStream(new FileOutputStream(resourcesFilename, false))
      try io.writeLibrary(lib, resOut) 
      finally resOut.close
      
      packager.pack(saveDir, homeDirectoryFile)
    } 
    
    def loadLibrary(libraryRef: ResourceLibraryRef): ResourceLibrary = {
      val unpackedDir = unpackLibrary(libraryRef)
      
      val schemaFile = getUnpackedLibraryPath(libraryRef) + "/" + schemaFileName
      val resourcesFile = getUnpackedLibraryPath(libraryRef) + "/" + resourcesFileName
      
      import meta._
      
      val schemaIn = new DataInputStream(new FileInputStream(schemaFile))
      val schema = try io.readLibrary(MetaSchema, schemaIn).asSchema 
                   finally schemaIn.close
               
      val resIn = new DataInputStream(new FileInputStream(resourcesFile))
      try io.readLibrary(schema, resIn)
      finally resIn.close() 
    }
  }
}



  