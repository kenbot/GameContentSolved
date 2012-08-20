package kenbot.gcsolved.editor.gui.sidebar
import scala.swing.BorderPanel
import kenbot.gcsolved.resource.types.RefType
import scala.swing.ListView
import scala.swing.tree.Tree
import scala.swing.tree.InternalTreeModel
import scala.swing.tree.TreeModel
import BorderPanel.Position._
import scala.swing.event.MouseClicked
import kenbot.gcsolved.resource.ResourceLibrary
import scala.swing.Frame
import scala.swing.Component
import scala.swing.event.Event
import kenbot.gcsolved.editor.gui.util.SearchEvent
import kenbot.gcsolved.editor.gui.util.SearchBar
import java.awt.Point

case class SelectionEvent[A](source: Component, selection: A) extends Event

object DoubleClick {
  def unapply(event: Event): Option[Point] = event match {
    case MouseClicked(_, pt, _, 2, _) => Some(pt)
    case _ => None
  }
}

class LibraryPage(val refTypes: Seq[RefType], val library: ResourceLibrary) extends BorderPanel {
  val searchBar = new SearchBar
  val treeView = new Tree(makeTreeModel) {
    renderer = Tree.Renderer(rt => rt.name + " (" + library.allResourcesByType(rt).size + ")")
  }
  treeView.expandAll()
  
  private def makeTreeModel = TreeModel(refTypes: _*)(getChildren)
  private def hasChildren(t: RefType) = getChildren(t).nonEmpty
  private def getChildren(t: RefType) = refTypes.filter(_.parent == t)
  
  listenTo(searchBar, treeView.mouse.clicks)
  
  reactions += {
    case SearchEvent(_, searchString) => 
      filterBy(searchString)
      revalidate()
      repaint()
      
    case DoubleClick(pt) => 
      val refType = treeView.getClosestPathForLocation(pt.x, pt.y).last
      publish(SelectionEvent(this, refType))
  }
  
  def filterBy(searchString: String) {
    def matches(t: RefType): Boolean = (t.name.toLowerCase contains searchString.toLowerCase) ||
                                       (getChildren(t) exists matches)
    
    treeView.model = makeTreeModel filter matches
    treeView.expandAll()
  }
  
  layout(searchBar) = North
  layout(treeView) = Center
}