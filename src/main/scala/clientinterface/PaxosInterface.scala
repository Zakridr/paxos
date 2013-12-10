package clientinterface

// swing junk
import swing.event._
import swing._
import swing.Swing.onEDT
import java.awt.{Color, Graphics2D, Point, geom, Dimension}

import paxutil._
// for local demo...
import multipaxos.Server

object PaxosInterface extends SimpleSwingApplication {
  val rows = 3
  val cols = 4

  val colourDelta = 20

  val btstrap = new Bootstrapper("client.csv")
  implicit val intermediator = new Intermediator(new ActorBag(btstrap.getParams4Remotes._2))

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
  def makeServerDisplay(replicaID : Symbol) = {
    new Panel {
      var currColour = new Colour(0, 0, 0)
      var blah = "no button clicked"
      background = new Colour(255, 255, 255)
      preferredSize = new Dimension(200,200)

      listenTo(intermediator)
      override def paintComponent(g: Graphics2D) = {
        super.paintComponent(g)
        g.setColor(currColour)
        val minDim = if (size.width < size.height) size.width else size.height
        // val widthOffset = this.size.width / 4
        // val heightOffset = this.size.height / 4
        g.fillArc(minDim / 4, minDim / 4, minDim / 2, minDim / 2, 0, 360) 
      }
      reactions += {
          // I just need to know the new state
        case Receive((`replicaID`, repState : DemoData)) => {
            println("GUI: display for replica " + replicaID + " got message")
            currColour = repState.state
            repaint()
        }
                               /*
        case Receive((`replicaID`, 'decR)) => currColour = currColour.changeRed(-colourDelta)
                                repaint()
        case Receive((`replicaID`, 'incG)) => currColour = currColour.changeGreen(colourDelta)
                                repaint()
        case Receive((`replicaID`, 'decG)) => currColour = currColour.changeGreen(-colourDelta)
                                repaint()
        case Receive((`replicaID`, 'incB)) => currColour = currColour.changeBlue(colourDelta)
                                repaint()
        case Receive((`replicaID`, 'decB)) => currColour = currColour.changeBlue(-colourDelta)
                               repaint()
                               */
      }
    }
  }

  // main method for GUIs
  // put reactions for buttons up top, for panes put them in the
  // pane itself
  def top = new MainFrame {
    title = "Paxos Colour Test"

    // *** this is for the local demo...
    val bstraps = List("local1.csv", "local2.csv", "local3.csv", "local4.csv", "local5.csv").map(fname => new Bootstrapper(fname))
    val initstate = new DemoData(new Colour(0, 0, 0))
    val servers = bstraps.map(bs => new Server(bs, initstate))
    servers.foreach(_.start)                  
    // *** end local demo stuff

    contents = new BoxPanel(Orientation.Vertical) {
      contents += new FlowPanel {
        contents += makeButtonBox(incrR, decrR)
        contents += makeButtonBox(incrG, decrG)
        contents += makeButtonBox(incrB, decrB)
      }
      contents += new GridPanel(2, 3) {
          contents += makeServerDisplay('r1)
          contents += makeServerDisplay('r2)
          contents += makeServerDisplay('r3)
          contents += makeServerDisplay('r4)
          contents += makeServerDisplay('r5)
      }
      //border = Swing.EmptyBorder(30, 30, 10, 30)
    }
  }
}
