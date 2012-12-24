package kenbot.gcsolved.core.types

import kenbot.gcsolved.core.meta.MetaFileType
import java.io.File
import scala.sys.error
import kenbot.gcsolved.core.meta.MetaAnyType

object FileType {
  val name = "File"
}

final case class FileType(path: String, extensions: String*) extends ResourceType(FileType.name + "(" + path + ")") {
  type Value = File
  
  def metaType: MetaAnyType = MetaFileType
  
  override def <:<(other: ResourceType): Boolean = other match {
    case AnyType => true
    case other: FileType => 
      def pathConforms = path startsWith other.path
      def extensionsConform = other.extensions forall {
        x => extensions exists { _ endsWith "." + x }
      }
      pathConforms && extensionsConform
      
    case _ => false
  }
  
  
  def asValue(a: Any): File = a match {
    case f: File => f
    case x => new File(x.toString)
  }
  
  override def getFailures(value: Any): List[String] =  {
    def wrongFileTypeError(f: File) = {
      val fname = f.getName.toLowerCase
      val valid = extensions exists {e => fname endsWith ("." + e.toLowerCase)}
      if (!valid) List("File must be one of " + extensions.mkString(", ") + ": " + f.getName) else Nil
    }
    val file = asValue(value)
    wrongFileTypeError(file)
  }
}
