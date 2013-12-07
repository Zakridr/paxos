package clientinterface

// swing junk
import swing.event._
import swing._
import swing.Swing.onEDT
import java.awt.{Color, Graphics2D, Point, geom, Dimension}

object PaxosInterface extends SimpleSwingApplication {
  val rows = 3
  val cols = 4

  val colourDelta = 20
  implicit val intermediator = new Intermediator()

  // buttons
  val incrR = makeColourButton("increase red", 'incR) 
  val incrG = makeColourButton("increase green", 'incG) 
  val incrB = makeColourButton("increase blue", 'incB) 
  val decrR = makeColourButton("decrease red", 'decR) 
  val decrG = makeColourButton("decrease green", 'decG) 
  val decrB = makeColourButton("decrease blue", 'decB) 

  def makeColourButton(label : String, msg : Symbol) = {
    new Button {
      text = label

      listenTo(this)
      reactions += {
        case ButtonClicked(b) => 
          Send(msg)
      }
    }
  }

  def makeButtonBox(b1 : Button, b2 : Button) = {
    new BoxPanel(Orientation.Vertical) {
      contents += b1
      contents += b2
    }
  }

  // makes a pane that draws in response 
  // to messages from the given server
  def makeServerDisplay(servername : String) = {
    new Panel {
      var currColor = Color.black
      var blah = "no button clicked"
      background = Color.white
      preferredSize = new Dimension(200,200)

      listenTo(intermediator)
      override def paintComponent(g: Graphics2D) = {
        super.paintComponent(g)
        g.setColor(currColor)
        val widthOffset = this.size.width / 4
        val heightOffset = this.size.height / 4
        g.fillArc(widthOffset, heightOffset, this.size.width / 2, this.size.height / 2, 0, 360) 
      }
      reactions += {
        case Receive('incR) => currColor = new Color(currColor.getRed() + colourDelta,
                                                            currColor.getGreen(),
                                                            currColor.getBlue())
                                       repaint()
        case Receive('decR) => currColor = new Color(currColor.getRed() - colourDelta,
                                                            currColor.getGreen(),
                                                            currColor.getBlue())
                                       repaint()
      }
    }
  }

  // main method for GUIs
  // put reactions for buttons up top, for panes put them in the
  // pane itself
  def top = new MainFrame {
    title = "Paxos Colour Test"

    contents = new BoxPanel(Orientation.Vertical) {
      contents += new FlowPanel {
        contents += makeButtonBox(incrR, decrR)
        contents += makeButtonBox(incrG, decrG)
        contents += makeButtonBox(incrB, decrB)
      }
      contents += makeServerDisplay("blah")
      //border = Swing.EmptyBorder(30, 30, 10, 30)
    }
  }
}