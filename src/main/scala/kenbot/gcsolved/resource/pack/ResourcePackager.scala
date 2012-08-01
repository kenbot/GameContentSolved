package kenbot.gcsolved.resource.pack

import java.io.File



trait ResourcePackager {
  
  def packageExtension: String
  def pack(unpackedDir: File, outputLocation: File): File
  def unpack(packageFile: File, outputLocation: File): File
  
  protected def requireValidPackableDirectory(unpackedDir: File) {
    require(unpackedDir.exists, "Can't package non-existent directory: " + unpackedDir)
    require(unpackedDir.isDirectory, "Can't package, expecting a directory: " + unpackedDir)
  }
  
  protected def requireValidUnpackableFile(packageFile: File) {
    require(packageFile.exists, "Can't unpack non-existent package file: " + packageFile)
    require(packageFile.isFile, "Can't unpack a directory: " + packageFile)
    require(packageFile.getName endsWith ("." + packageExtension), 
        "Can't unpack non-existent package file: " + packageFile)
  }
}
