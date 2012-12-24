package kenbot.gcsolved.editor.gui.sidebar
import scala.swing.BorderPanel
import kenbot.gcsolved.core.types.RefType
import scala.swing.ListView
import scalaswingcontrib.tree.Tree
import scalaswingcontrib.tree.InternalTreeModel
import scalaswingcontrib.tree.TreeModel
import BorderPanel.Position._
import scala.swing.event.MouseClicked
import kenbot.gcsolved.core.ResourceLibrary
import scala.swing.Frame
import scala.swing.Component
import scala.swing.event.Event
import kenbot.gcsolved.core.ResourceSchema
import kenbot.gcsolved.core.types.UserType
import kenbot.gcsolved.core.types.ValueType
import kenbot.gcsolved.core.types.AnyRefType
import kenbot.gcsolved.core.types.AnyValueType
import kenbot.gcsolved.editor.gui.util.SearchEvent
import kenbot.gcsolved.editor.gui.util.SearchBar



class SchemaPage(val schema: ResourceSchema) extends BorderPanel {
  
  val searchBar = new SearchBar
  val treeView = new Tree(makeTreeModel)
  treeView.expandAll()
  
  object RefTypes {
    override def toString() = "Entities"
  }
  case object ValueTypes {
    override def toString() = "Data Structures"
  }
  case object SelectOneTypes {
    override def toString() = "Lists"
  }
  
  private def makeTreeModel = TreeModel[Any](RefTypes, ValueTypes, SelectOneTypes)(getChildren)
  private def getChildren(a: Any) = a match {
    case RefTypes => schema.userRefTypes filter { _.parent == AnyRefType }
    case ValueTypes => schema.userValueTypes filter { _.parent == AnyValueType }
    case SelectOneTypes => schema.selectOneTypes
    case rt: RefType => schema.userRefTypes filter { _.parent == rt }
    case vt: ValueType => schema.userValueTypes filter { _.parent == vt }
    case _ => Seq.empty
  }
  
  listenTo(searchBar, treeView.mouse.clicks)
  
  reactions += {
    case SearchEvent(_, str) => 
      filterBy(str)
      revalidate()
      repaint()
      
    case MouseClicked(_, pt, _, 2, _) => 
      val refType = treeView.getClosestPathForLocation(pt.x, pt.y).last
      publish(SelectionEvent(this, refType))
  }
  
  
  def filterBy(searchString: String) {
    def matches(a: Any): Boolean = (a.toString.toLowerCase contains searchString) ||
                                   (getChildren(a) exists matches)
    
    treeView.model = makeTreeModel filter matches
    treeView.expandAll()
  }
  
  layout(searchBar) = North
  layout(treeView) = Center
}