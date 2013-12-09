package multipaxos

import scala.actors.remote._
import scala.actors.remote.RemoteActor._
import paxutil._

object ActFact {

    def getHandle(id : Symbol) = select(Node("127.0.0.1", 9010), id)
}

// hardcoded to always use c1 for now
object CommandFactory {
    def makeCommand(num : Int, op : Symbol) = new Command(new ActorData("127.0.0.1", 9010, Symbol("c1")), num, op)
}
