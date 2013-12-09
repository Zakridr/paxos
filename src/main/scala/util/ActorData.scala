package paxutil

import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node

class ActorData(h: String, p: Int, i : Symbol) extends Serializable {
    val host = h
    val port = p
    val id  = i

    def makeActorHandle() = select(Node(host, port), id)

    override def equals(other : Any) : Boolean = 
        other match {
            case that : ActorData => (host == that.host &&
                                      port == that.port &&
                                      id   == that.id)
            case _                => false
        }
}
