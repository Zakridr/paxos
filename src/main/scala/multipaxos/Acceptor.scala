package multipaxos
import scala.actors._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor.{alive, register}

import paxutil._

class Acceptor(params:ActorData, ls : ActorBag) extends Actor{

    val port = params.port
    val id = params.id
    val leaders = ls

    var acceptor_b_num = new B_num(-1, Symbol("0"))
    var acceptor_accepted = new PvalueList()

    def prune(slot_num:Int) : PvalueList = {
        if(acceptor_accepted.size()==0){
            return acceptor_accepted
        }
        else{
            return acceptor_accepted.filter_By_s_num(slot_num)
        }
    }

    def Acceptor_fun(){
        // as acceptor
        receive{               
            case ("prepare request", l_id:Symbol, b:B_num, scout_id : Symbol, slot_num:Int) =>{
                //println("I'm server:"+ id +" I got prepare request")
                if(b > acceptor_b_num){
                    acceptor_b_num = b
                }

                //prune the pvalues according to the slot_num
                val pruned_accepted = prune(slot_num)
                sender!("prepare reply", id, acceptor_b_num, pruned_accepted)
                Console.println("As accecptor server: " + id + " reply prepare request with b_num:" + acceptor_b_num.toString())
                Console.println("And I attached my accepted pvalues:" )
                pruned_accepted.print()

            }//end case
            case ("accept request", l_id: Symbol, p:Pvalue, commdr_id :Symbol) =>{
                //println("server "+ id + " find a accept request match")

                if(p.get_B_num() >= acceptor_b_num){
                    acceptor_b_num = p.get_B_num()
                    acceptor_accepted.put(p)
                }
                //Note here if use reply or sender!(msg), the Leader_Commander will not able to receive this msg
                //Zach: when I try to make classes which extend Serializable, I get runtime errors... we need a different solution then
                // TODO
                sender!("accept reply", this, acceptor_b_num)
                //println("hello, hello, hello, I receive  accept request from " + sender)
                //Console.println("As accecptor server: " + id + " reply accept request with b_num:" + acceptor_b_num.toString())
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
