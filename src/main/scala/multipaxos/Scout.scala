package multipaxos

import scala.actors._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor.{alive, register}

import paxutil._

 //leader function scout
 // for now, I guess it's ok to leave the leader as a paramter, since he's local...
class Scout(params : ActorData, l : Leader, l_acceptors : ActorBag,  b:B_num, slot_num: Int) extends Actor{
    val id = params.id
    val port = params.port
    val acc = l_acceptors.actorsToList 

    var scout_waitfor = l_acceptors.symbolsToList
    var pvalues = new PvalueList()

    for(s <- acc){
        s ! ("prepare request", b, slot_num, params)
        //Console.println("As scout leader server: " + l.name + " I send prepare request to acceptor: " + s.name +" with b_num:"+b.toString())
    }
    def act(){
        alive(params.port)
        register(params.id, self)
        println("I'm scout with id " + params.id)

        while(true){
            receive{
                //sender!("prepare reply", id, acceptor_b_num, pruned_accepted)
                case ("prepare reply", acc_id : Symbol, b1:B_num, r:PvalueList) =>{
                    //Console.println("As scout leader server: " + l.name + " I got repare request from" + s.name+ " with its accepted pvaluelist:")
                    //r.print()
                    println("scout got message from acceptor " + acc_id)
                    if(b.equal(b1)){
                        pvalues.putList(r)
                        scout_waitfor = scout_waitfor diff List(acc_id)
                        ////println("now waitfor length is: "+ scout_waitfor.length + " acceptors length/2 is: "+ acc.length/2)
                        if(scout_waitfor.length < (acc.length/2)){
                            //println("I'm leader, in Scout, I send adopted")
                            l!("adopted", b, pvalues)
                            exit()
                        }
                    }
                    // TODO make sure ifs are lined up properly
                    else{
                        l!("Sorry", b1)
                        exit()
                    }
                }//end case
            }//end receive
        }// end while
    }
}//end Scout
