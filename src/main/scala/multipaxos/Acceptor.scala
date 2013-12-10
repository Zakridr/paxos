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
            case ("prepare request", b:B_num, slot_num:Int, scoutdata : ActorData) =>{
                val scout = scoutdata.makeActorHandle
                //println("I'm server:"+ id +" I got prepare request")
                if(b > acceptor_b_num){
                    acceptor_b_num = b
                }

                //prune the pvalues according to the slot_num
                val pruned_accepted = prune(slot_num)
                scout!("prepare reply", id, acceptor_b_num, pruned_accepted)
                //Console.println(id + ": PREPARE REPLY, ballot: " + acceptor_b_num.toString + ", scout: " + scoutdata.id)
                //Console.println("And I attached my accepted pvalues:" )
                //pruned_accepted.print()

            }//end case
            case ("accept request", p:Pvalue, cmmdrdata : ActorData) =>{
                //println("server "+ id + " find a accept request match")
                val cmmdr = cmmdrdata.makeActorHandle

                if(p.get_B_num() >= acceptor_b_num){
                    acceptor_b_num = p.get_B_num()
                    acceptor_accepted.put(p)
                }
                // TODO using sender here
                println("hello, hello, hello, I receive  accept request with slot_num "+p.s_num )
                cmmdr ! ("accept reply", id, acceptor_b_num)
                
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
