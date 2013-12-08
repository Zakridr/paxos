package multipaxos
import scala.actors._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor.{alive, register}

import paxutil._

class Acceptor(primeLeader:ActorData, params:ActorData, ls : ActorBag) extends Actor{

    val port = params.port
    val id = params.id
    val leaders = ls
        
    var acceptor_b_num = new B_num(-1, primeLeader.id)
    var acceptor_accepted = new PvalueList()

    def Acceptor_fun(){
        // as acceptor
        receive{               
            case ("prepare request", l_id:Symbol, b:B_num) =>{
                println("I'm server:"+ id +" I got prepare request")
                if(b > acceptor_b_num){
                    acceptor_b_num = b
                }
                leaders.getActBySym(l_id) match {
                    case Some(l) => l!("prepare reply", id, acceptor_b_num, acceptor_accepted)
                                    println("As accecptor server: " + id + " reply prepare request with b_num:" + acceptor_b_num.toString())
                                    println("And I attached my accepted pvalues:" )
                                    acceptor_accepted.print()
                    case None    => println("Uh oh, acceptor failed to find reference for leader")
                }

            }//end case
            case ("accept request", l_id:Symbol, p:Pvalue) =>{
                println("server "+ id + " find a accept request match")
                if(p.get_B_num() >= acceptor_b_num){
                    acceptor_b_num = p.get_B_num()
                    acceptor_accepted.put(p)
                }

                leaders.getActBySym(l_id) match {
                    case Some(l : AbstractActor) => l!("accept reply", id, acceptor_b_num)
                                    println("As accecptor server: " + id + " reply accept request with b_num:" + acceptor_b_num.toString())
                    case None    => println("Uh oh, acceptor failed to find reference for leader")
                }
            }// end case

        }//end receive
    }//end Acceptor_fun

    def act(){
        alive(port)
        register(id, self)
        
        println("acceptor: " + id + "Started")
        while(true){
            Acceptor_fun()
        }//end while

    }//end act
}//end class
