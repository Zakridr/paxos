package paxutil

import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node

class ActorData(h: String, p: Int, i : Symbol) {
    val host = h
    val port = p
    val id  = i

    def makeActorHandle() = select(Node(host, port), id)
}
