package clientinterface

// actor junk
import actors._
import actors.Actor
import actors.Actor._
import actors.remote._
import actors.remote.RemoteActor._

//swing junk
import swing._
import swing.event._
import swing.Swing.onEDT

case class Send(event: Any)(implicit intermediator: Intermediator) {
  intermediator ! this
}
case class Receive(event: Any) extends Event

class Intermediator extends Actor with Publisher {
  start()

  def act() {
    alive(9010)
    register('monitor, self)
    loop {
      react {
        case Send(evt) => onEDT(publish(Receive(evt)))
                          println("gui did something")
        case evt => onEDT(publish(Receive(evt)))
      }
    }
  }
}
