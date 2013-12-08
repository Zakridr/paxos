package multipaxos
import scala.actors._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor.{alive, register}

import paxutil.ActorData

class Acceptor(primeLeader:ActorData, params:ActorData) extends Actor{

    val port = params.port
    val id = params.id
        
    var acceptor_b_num = new B_num(-1, primeLeader.id)
    var acceptor_accepted = new PvalueList()

    def Acceptor_fun(){
        // as acceptor
        receive{               
            case ("prepare request", l:Leader, b:B_num) =>{
                println("I'm server:"+ id +" I got prepare request")
                if(b > acceptor_b_num){
                    acceptor_b_num = b
                }
                // TODO sending an actor
                l!("prepare reply", this, acceptor_b_num, acceptor_accepted)
                println("As accecptor server: " + id + " reply prepare request with b_num:" + acceptor_b_num.toString())
                println("And I attached my accepted pvalues:" )
                acceptor_accepted.print()

            }//end case
            case ("accept request", l:Leader, p:Pvalue) =>{
                println("server "+ id + " find a accept request match")
                if(p.get_B_num() >= acceptor_b_num){
                    acceptor_b_num = p.get_B_num()
                    acceptor_accepted.put(p)
                }

                // TODO sending an actor
                l!("accept reply", this, acceptor_b_num)
                println("As accecptor server: " + id + " reply accept request with b_num:" + acceptor_b_num.toString())
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
