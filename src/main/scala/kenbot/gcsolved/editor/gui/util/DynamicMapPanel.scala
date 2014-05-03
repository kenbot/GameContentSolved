package kenbot.gcsolved.editor.gui.util

import java.awt.Color

import scala.swing.{Alignment, BoxPanel, Component, GridPanel, Orientation}
import scala.swing.Swing.pair2Dimension
import scala.swing.event.ButtonClicked

import javax.swing.BorderFactory


object DynamicMapPanel {
  trait Support[K, V] {
    type KeyComponent <: Component
    type ValueComponent <: Component
    
    def newKeyComponent(): KeyComponent
    def newValueComponent(): ValueComponent
    def getKey(kc: KeyComponent): Option[K]
    def getValue(vc: ValueComponent): Option[V]
    def makeKeyComponent(key: K): KeyComponent
    def makeValueComponent(value: V): ValueComponent
    
    final def newComponents() = (newKeyComponent, newValueComponent)
    final def getValues(comps: (KeyComponent, ValueComponent)) = (getKey(comps._1), getValue(comps._2))
    final def makeComponents(kv: (K,V)) = (makeKeyComponent(kv._1), makeValueComponent(kv._2))
  }
}

class DynamicMapPanel[K, V]
    (entries: Map[K, V])
    (implicit support: DynamicMapPanel.Support[K, V])
    extends NestedBorderPanel {
  
  def this()(implicit support: DynamicMapPanel.Support[K, V]) = this(Map())(support)
  
  import support._
  
  def keyPanels: IndexedSeq[Component] = pairPanels.map(_.keyPanel)
  def valuePanels: IndexedSeq[Component] = pairPanels.map(_.valuePanel)
  
  private var pairPanels: IndexedSeq[PairPanel] = makePairPanels(entries)
  private val listPanel = new BoxPanel(Orientation.Vertical) {
    border = BorderFactory.createEmptyBorder(5,5,5,5)
  }
  
  private val initialAddButton = new AddButton {
    text = "     Add an entry..."
    preferredSize = (200, 32)
    horizontalAlignment = Alignment.Left
    reactions += {
      case ButtonClicked(_) => installPairPanels(_ :+ PairPanel(newComponents)) 
    }
  }
  
  private val addPanel = new NestedBorderPanel {
    border = BorderFactory.createEmptyBorder(4, 4, 4, 4)
    west = initialAddButton
  }
  
  private def PairPanel(panels: (KeyComponent, ValueComponent)) = new PairPanel(panels._1, panels._2)
  
  private class PairPanel(val keyPanel: KeyComponent, val valuePanel: ValueComponent) extends NestedBorderPanel {
    private val addButton = new AddButton {
      preferredSize = null
    }
    private val deleteButton = new DeleteButton
    
    listenTo(addButton, deleteButton)

    override def enabled: Boolean = addButton.enabled
    override def enabled_=(b: Boolean) {
      addButton.enabled = b
      deleteButton.enabled = b
    }
    
    west = new NestedBorderPanel {
      north = new GridPanel(1,2) {
        contents += addButton
        contents += deleteButton
      }
    }
    center = new NestedBorderPanel {
      west = new NestedBorderPanel {
        north = keyPanel
        border = BorderFactory.createEmptyBorder(0, 0, 0, 10)
      }
      center = valuePanel
    }
    
    reactions += {
      case ButtonClicked(`addButton`) => installPairPanels(_ :+ PairPanel(newComponents))
      case ButtonClicked(`deleteButton`) => installPairPanels(_ filterNot eq) 
    }
  }
  
  private def makePairPanels(entries: Map[K, V]) = entries.toIndexedSeq map makeComponents map PairPanel
  
  private def installPairPanels(modifyPanels: IndexedSeq[PairPanel] => IndexedSeq[PairPanel] = identity) {
    pairPanels = modifyPanels(pairPanels)
    listPanel.contents.clear()
    pairPanels foreach listPanel.contents.+=
      
    if (pairPanels.isEmpty) {
      listPanel.contents += addPanel
    }
    
    revalidate()
    repaint()
  }
  
  override def enabled: Boolean = initialAddButton.enabled
  override def enabled_=(b: Boolean) {
    initialAddButton.enabled = b
    pairPanels.foreach(_.enabled = b)
  }
  
  def values: Map[K, V] = {
    val optionalValues = pairPanels.map(p => getKey(p.keyPanel) -> getValue(p.valuePanel))
    val definedValuesOnly = optionalValues.collect { case (Some(k), Some(v)) => k -> v }
    definedValuesOnly.toMap
  }
  
  def values_=(entries: Map[K, V]) {
    pairPanels = makePairPanels(entries)
    installPairPanels()
  }
  
  north = listPanel
  border = BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD))
  installPairPanels()
}