package kenbot.gcsolved.resource.pack

import java.io.File
import java.util.zip.GZIPOutputStream
import java.io.FileOutputStream
import java.io.FileInputStream
import java.util.zip.ZipOutputStream
import java.io.BufferedOutputStream
import java.io.BufferedInputStream
import java.util.zip.ZipEntry
import scala.annotation.tailrec
import java.util.zip.ZipFile


object DefaultResourcePackager extends DefaultResourcePackager


trait DefaultResourcePackager extends ResourcePackager {
  val packageExtension: String = "content"
    
  def pack(unpackedDir: File, outputLocation: File): File = {
    requireValidPackableDirectory(unpackedDir)
    
    val bufferSize = 2048
    val data = new Array[Byte](bufferSize)
    val environmentDir = Option(unpackedDir.getParent) getOrElse "."
    val packedFile = new File(outputLocation.getPath + "/" + unpackedDir.getName + "." + packageExtension)
    lazy val zipOut = new ZipOutputStream(
        new BufferedOutputStream(
            new FileOutputStream(packedFile)))
    
    val outsidePath = unpackedDir.getPath dropRight unpackedDir.getName.length
    
    def zipFile(file: File) {
      val internalFilePath = file.getPath drop outsidePath.length
      zipOut.putNextEntry(new ZipEntry(internalFilePath))
      
      lazy val fileInput = new BufferedInputStream(new FileInputStream(file), bufferSize)
      try {
        var count = fileInput.read(data, 0, bufferSize)
        while (count != -1) {
          zipOut.write(data, 0, count)
          count = fileInput.read(data, 0, bufferSize)
        }
      }
      finally fileInput.close()
    }
    
    def zipDir(dir: File) {
      val childFiles = dir.listFiles() ensuring (_ != null)
      childFiles foreach zip
    }
    
    def zip(fileOrDir: File) {
      if (fileOrDir.isDirectory) zipDir(fileOrDir)
      else zipFile(fileOrDir)
    }
    
    try zip(unpackedDir) 
    finally zipOut.close()
    
    packedFile ensuring (f => f.exists && f.isFile)
  }
  
  
  def unpack(packageFile: File, outputLocation: File): File = {
    requireValidUnpackableFile(packageFile)
        
    val bufferSize = 2048
    
    lazy val zipFile = new ZipFile(packageFile)
    try {
      val e = zipFile.entries
      val basePath = outputLocation.getPath + "/"
      val unpackedDir = new File(basePath + packageFile.getName.takeWhile(_ != '.'))
      
      while (e.hasMoreElements) {
        val entry = e.nextElement.asInstanceOf[ZipEntry]
        val buffer = new Array[Byte](bufferSize)
        
        val fileIn = new BufferedInputStream(zipFile.getInputStream(entry))
        try {
          val outFile = new File(basePath + entry.getName)
          if (entry.isDirectory) {
            outFile.mkdirs()
          }
          else {
            
            val packageDir = outFile.getParentFile
            require(packageDir.exists || packageDir.mkdirs(), "Could not create directories: " + outFile.getParentFile)
            val dirOut = new BufferedOutputStream(
                new FileOutputStream(outFile.getPath), bufferSize)
      
            try {
              var count = fileIn.read(buffer, 0, bufferSize)
              while (count != -1) {
                dirOut.write(buffer, 0, count);
                count = fileIn.read(buffer, 0, bufferSize)
              }
            }
            finally {
              dirOut.flush()
              dirOut.close()
            }
          }
        }
        finally fileIn.close()
      }
      unpackedDir ensuring (d => d.exists && d.isDirectory)
    }
    finally zipFile.close()
  }
}