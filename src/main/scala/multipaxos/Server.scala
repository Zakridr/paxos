package multipaxos
import scala.io.Source
import scala.actors._
import scala.actors.Actor._
import scala.concurrent._


class Server(sname: String) extends Actor{
    val name = sname
    var id = -1
    val acceptor = new Acceptor(name)
    val replica = new Replica(name)
    val leader = new Leader(name)
    var servers = List[Server]()

    def init_servers(inits: List[Server]) = {
      servers = inits
      id = getId(servers)
      acceptor.init(servers)
      replica.init(servers,getLeaders(servers))
      leader.init(getReplicas(servers), getAcceptors(servers), getLeaders(servers),id)

    }
 
  
    def getId(ss:List[Server]):Int={
        return ss.indexOf(this)

    }

    def getLeaders(ss: List[Server]):List[Leader] = {
        var leaders = List[Leader]()
        ss.foreach{e=>{leaders :+= e.leader}}
        return leaders
    }


    def getReplicas(ss: List[Server]):List[Replica] = {
        var replicas = List[Replica]()
        ss.foreach{e=>{replicas :+= e.replica}}
        return replicas
    }

     def getAcceptors(ss: List[Server]):List[Acceptor] = {
        var acceptors = List[Acceptor]()
        ss.foreach{e=>{acceptors :+= e.acceptor}}
        return acceptors
    }
    
    def act(){
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

     def printArray()={
        replica.printArray()

    }

    }