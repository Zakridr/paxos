package multipaxos

import scala.actors._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor.{alive, register}

import paxutil.ActorData
import paxutil.Bootstrapper


class Server(primeLeader : ActorData, bs : Bootstrapper)  extends Actor{
    val port = bs.getParams4Local._1.head.port
    val id = bs.getParams4Local._1.head.id

    val localAcceptor = bs.getParams4Local._4.head
    val remoteAcceptors = bs.getParams4Remotes._4
    val localLeader = bs.getParams4Local._3.head
    val remoteLeaders = bs.getParams4Remotes._3
    val localReplica = bs.getParams4Local._2.head
    val remoteReplicas = bs.getParams4Local._2

                 // I don't think the acceptors need to know who the prime leader is...
    val acceptor = new Acceptor(primeLeader, localAcceptor)
    val replica = new Replica(primeLeader, localReplica, remoteLeaders.map(_.makeRemoteActor), localLeader.makeRemoteActor)
    val leader = new Leader(primeLeader,
                            localLeader,
                            (localReplica :: remoteReplicas).map(_.makeRemoteActor),
                            (localAcceptor :: remoteAcceptors).map(_.makeRemoteActor))

    def getAcceptor():Acceptor={ return acceptor}

    def getReplicas():Replica={return replica}

    def isLeader():Boolean = primeLeader.id == localLeader.id

    def getReplicas(ss: List[Server]):List[Replica] = ss.map( s => s.replica)

    def getAcceptors(ss: List[Server]):List[Acceptor] = ss.map( s => s.acceptor)

    def act(){
        alive(port)
        register(id, self)

        acceptor.start
        replica.start  
        if(isLeader()){leader.start}
            
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
