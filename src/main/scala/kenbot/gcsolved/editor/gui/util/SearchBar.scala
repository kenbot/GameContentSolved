package kenbot.gcsolved.editor.gui.util
import scala.swing.BorderPanel.Position.Center
import scala.swing.BorderPanel.Position.East
import scala.swing.Swing.onEDT
import scala.swing.event.ButtonClicked
import scala.swing.event.Event
import scala.swing.event.KeyPressed
import scala.swing.BorderPanel
import scala.swing.Button
import scala.swing.Publisher
import scala.swing.TextField



case class SearchEvent(source: SearchBar, searchString: String) extends Event

object SearchBar {
  def apply(callback: String => Unit) = new SearchBar {
    reactions += {
      case SearchEvent(_, str) => callback(str)
    }
  }
}
class SearchBar extends BorderPanel with Publisher {
  val searchButton = new Button("Go")
  val searchField = new TextField with BlankText {
    blankText = "Search..."
    TextFields.autoSelectOnFocus(this)
  }
  
  listenTo(searchButton, searchField.keys, searchField)
  
  reactions += {
    case ButtonClicked(_) => publishSearch(text)
    case kp: KeyPressed => onEDT { 
      publishSearch(text)
    }
  }
  
  def text = searchField.text.trim
  
  private def publishSearch(str: String) { publish(SearchEvent(this, searchField.text.trim)) }
  
  layout(searchField) = Center
  layout(searchButton) = East
}