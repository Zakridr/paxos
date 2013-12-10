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

// local junk
import paxutil._
import multipaxos.Command

case class Send(event: Any)(implicit intermediator: Intermediator) {
  intermediator ! this
}
case class Receive(event: Any) extends Event

class Intermediator (replicas : ActorBag) extends Actor with Publisher {
    val mydata = new ActorData("127.0.0.1", 9010, 'c1)

    var replicaCmdIds = replicas.symbolsToList.map( repid => (repid, -1)).toMap
    var cmmdNum = 0

    start()
    // client id is hardcoded!
    // TODO
    def act() {
        alive(mydata.port)
        register(mydata.id, self)

        loop {
            react {
                // TODO

                // so send... is from the GUI? 
                // when I receive this, broadcast to all replicas
                case Send(op : Symbol) => {
                    cmmdNum += 1
                    println("INTERMEDIATOR: sending new request, command number id is: " + cmmdNum + ", op is: " + op)
                    val cmd = new Command(mydata, cmmdNum, op)
                    for (r <- replicas.actorsToList) {
                        println("INTERMEDIATOR: sending to: " + r)
                        r ! ("request", cmd)
                    }
                }
                // this is a message from a replica. check if it's higher than any cmid we've received thus far
            //client ! ("response", c.cmid, state, id)
                case ("response", cmmdid : Int, repstate : DemoData, repid : Symbol) => {
                    if (cmmdid > replicaCmdIds(repid)) {
                        println("INTERMEDIATOR: got message from replica " + repid)
                        replicaCmdIds = replicaCmdIds + ((repid, cmmdid))
                        onEDT(publish(Receive(repid, repstate)))
                    }
                }
            }
        }
    }
}
