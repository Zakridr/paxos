package multipaxos

import scala.actors.remote._
import scala.actors.remote.RemoteActor._
import paxutil._

object ActFact {

    def getHandle(id : Symbol) = select(Node("127.0.0.1", 9010), id)
}

object CommandFactory {
    def makeCommand(num : Int) = new Command(new ActorData("127.0.0.1", 9010, Symbol("c1")), num, Symbol("write" + num))
}
