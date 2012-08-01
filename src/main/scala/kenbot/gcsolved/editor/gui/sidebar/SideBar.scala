package kenbot.gcsolved.editor.gui.sidebar

import scala.swing.TabbedPane.Page
import scala.swing.Button
import scala.swing.Publisher
import scala.swing.TabbedPane

import kenbot.gcsolved.editor.Settings

class SideBar(settings: Settings) extends TabbedPane with Publisher {
  val aboutPage = new AboutPage(settings.currentLibrary)
  val schemaPage = new SchemaPage(settings.schema)
  val libraryPage = new LibraryPage(settings.schema.userRefTypes, settings.currentLibrary)
  val otherLibrariesPage = new OtherLibrariesPage(settings.environment)
  
  pages += new Page("About", aboutPage)
  pages += new Page("Schema", schemaPage)
  pages += new Page("Library", libraryPage)
  pages += new Page("Files", new Button)
  pages += new Page("Other Libraries", new Button)
}