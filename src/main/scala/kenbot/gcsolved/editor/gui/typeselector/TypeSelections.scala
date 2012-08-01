package kenbot.gcsolved.editor.gui.typeselector
import scala.sys.error
import kenbot.gcsolved.resource.types.DoubleType
import kenbot.gcsolved.resource.types.IntType
import kenbot.gcsolved.resource.types.ResourceType
import kenbot.gcsolved.resource.types.StringType
import kenbot.gcsolved.resource.types.BoolType
import kenbot.gcsolved.resource.types.FileType
import kenbot.gcsolved.resource.types.AnyRefType
import kenbot.gcsolved.resource.types.AnyType
import kenbot.gcsolved.resource.types.ListType
import kenbot.gcsolved.resource.types.AnyValueType
import kenbot.gcsolved.resource.types.MapType
import kenbot.gcsolved.resource.types.RefType
import kenbot.gcsolved.resource.types.UserType
import kenbot.gcsolved.resource.ResourceSchema


object TypeSelection {
  val systemTypes: List[TypeSelection] = List(AnySelection, AnyRefSelection, AnyValueSelection,
                         IntSelection, StringSelection, BoolSelection, 
                         DoubleSelection, FileSelection, ListSelection, MapSelection)

  def systemTypesUnder(t: ResourceType): List[TypeSelection] = t match {
    case AnyType => systemTypes
    case AnyRefType => List(AnyRefSelection)
    case AnyValueType => List(AnyValueSelection)
    case ut: UserType => error("No system type selection found for " + ut)
    case rt: ResourceType => List(forResourceType(rt))
  }
                         
  def forResourceType(t: ResourceType): TypeSelection = t match {
    case AnyType => AnySelection
    case IntType => IntSelection
    case DoubleType => DoubleSelection
    case StringType => StringSelection
    case BoolType => BoolSelection
    case FileType(_, _*) => FileSelection
    case ListType(_, _) => ListSelection
    case MapType(_,_) => MapSelection
    case ut: UserType => UserTypeSelection(ut)
    case x => error("No type selection found for " + x)
  }
}

sealed abstract class TypeSelection(val name: String) {
  def getResourceType(refinementWidget: Option[TypeRefinementWidget]): ResourceType
  def makeRefinementWidget(schema: ResourceSchema): Option[TypeRefinementWidget]
  override def toString() = name
}

sealed abstract class RefinedTypeSelection[W <: TypeRefinementWidget](name: String, make: ResourceSchema => W)(f: W => ResourceType) 
    extends TypeSelection(name) {
  
  override def makeRefinementWidget(schema: ResourceSchema): Some[W] = Some(make(schema))
  
  override def getResourceType(widget: Option[TypeRefinementWidget]) = widget match {
    case Some(rw: W) => f(rw)
    case _ => error(name + " type needs a refinement widget")
  }
}

sealed abstract class UnrefinedTypeSelection(resourceType: ResourceType)
    extends TypeSelection(resourceType.name) {
  
  override def getResourceType(rw: Option[TypeRefinementWidget]) = resourceType
  override def makeRefinementWidget(schema: ResourceSchema) = None
}

object AnySelection extends UnrefinedTypeSelection(AnyType)
object AnyRefSelection extends UnrefinedTypeSelection(AnyRefType)
object AnyValueSelection extends UnrefinedTypeSelection(AnyValueType)
object DoubleSelection extends UnrefinedTypeSelection(DoubleType)
object StringSelection extends UnrefinedTypeSelection(StringType)
object BoolSelection extends UnrefinedTypeSelection(BoolType)

object IntSelection extends RefinedTypeSelection(IntType.name, _ => new IntRefinementWidget)(
    rw => IntType(rw.min, rw.max))
    
object FileSelection extends RefinedTypeSelection(FileType.name, _ => new FileRefinementWidget)(
    rw => FileType(rw.category, rw.extensions: _*))

object ListSelection extends RefinedTypeSelection(ListType.name, s => new ListRefinementWidget(s))(
    rw => ListType(rw.elementType))

object MapSelection extends RefinedTypeSelection(MapType.name, s => new MapRefinementWidget(s))(
    rw => MapType(rw.keyType, rw.valueType))
    
case class UserTypeSelection(userType: ResourceType with UserType) extends UnrefinedTypeSelection(userType)
