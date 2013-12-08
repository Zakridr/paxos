package multipaxos

import scala.actors._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor.{alive, register}

import paxutil._

class Server(bs : Bootstrapper)  extends Actor{
    val port = bs.getParams4Local._1.head.port
    val id = bs.getParams4Local._1.head.id

    val localAcceptor = bs.getParams4Local._4.head
    val remoteAcceptors = bs.getParams4Remotes._4
    val localLeader = bs.getParams4Local._3.head
    val remoteLeaders = bs.getParams4Remotes._3
    val localReplica = bs.getParams4Local._2.head
    val remoteReplicas = bs.getParams4Remotes._2

                 // I don't think the acceptors need to know who the prime leader is...
    val acceptor = new Acceptor(localAcceptor, new ActorBag(localLeader :: remoteLeaders))
    val replica = new Replica(localReplica, 
                              new ActorBag(remoteLeaders), 
                              localLeader.makeActorHandle)
    val leader = new Leader(localLeader,
                            new ActorBag(localReplica :: remoteReplicas),
                            new ActorBag(localAcceptor :: remoteAcceptors))

    def getAcceptor():Acceptor = return acceptor

    def act(){
        alive(port)
        register(id, self)

        //println("Server " + id + " has " + (localReplica :: remoteReplicas).length + " replicas")

        acceptor.start
        replica.start  
        leader.start
            
        while(true){
            receive{
                case ("request", c: Command) => {
                    replica !("request", c)
                }
                
            }
        }
    }

    def printArray()= replica.printArray()
}
