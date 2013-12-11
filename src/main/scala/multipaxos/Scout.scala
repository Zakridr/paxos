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


    // trying to fix timing issue
    // not sure what it is...?
    def messageHoldouts() = {
        for (accid <- scout_waitfor) {
            println(params.id + ": sending wakeup to accid:" + accid + ", ref: " + l_acceptors.getActBySym(accid))
            l_acceptors.getActBySym(accid) ! ("prepare request", b, slot_num, params)
        }
        //scout_waitfor.foreach( acc_id => l_acceptors.getActBySym(acc_id) ! ("prepare request", b, slot_num, params))
    }

    def act(){
        alive(params.port)
        register(params.id, self)
        println("I'm scout with id " + params.id)

        for(s <- acc){
            s ! ("prepare request", b, slot_num, params)
        }

        while(true){
            receiveWithin(500){
                case ("prepare reply", acc_id : Symbol, b1:B_num, r:PvalueList) =>{
                    //Console.println("As scout leader server: " + l.name + " I got repare request from" + s.name+ " with its accepted pvaluelist:")
                    //r.print()
                    //println("scout " + id + " got message from acceptor " + acc_id)
                    if(b.equal(b1)){
                        pvalues.putList(r)
                        scout_waitfor = scout_waitfor diff List(acc_id)
                        if(scout_waitfor.length < acc.length/2 + (acc.length % 2)){
                            l!("adopted", b, pvalues)
                            exit()
                        }
                    }
                    // TODO make sure ifs are lined up properly
                    else{
                        println(id + " : PRE-EMPTED")
                        l!("Sorry", b1)
                        exit()
                    }
                }//end case
                case TIMEOUT => messageHoldouts()
            }//end receive
        }// end while
    }
}//end Scout
