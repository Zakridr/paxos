package multipaxos

import scala.actors._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor.{alive, register}

import paxutil._

//this class is used in Leader for concurrency

class Commander(params : ActorData, l : Leader, l_replicas : ActorBag, l_acceptors : ActorBag, pv : Pvalue) extends Actor {
    //val id = Symbol(leaderparams.id + "c")
    //val port = leaderparams.port
    //val mydata = new ActorData(leaderparams.host, port, id)

    var waitfor = l_acceptors.symbolsToList
    val acc = l_acceptors.actorsToList
    val rep = l_replicas.actorsToList

    def act(){
        alive(params.port)
        register(params.id, self)

        for(s <- acc){
            s!("accept request", pv, params)
        }
        //println("!!!!!!!!!!!!!!aaaaaaaaaa"+params.id + ": STARTED")
        while(true){
            receive{
                case ("accept reply", acc_id : Symbol, b : B_num) => {
                    //println("hello hello hello I got one accept reply with s_num "+ pv.s_num)
                    if(b.equal(pv.get_B_num())){
                        waitfor = waitfor diff List(acc_id)
                        //println("commander's waitfor length: "+ waitfor.length)
                        if(waitfor.length <= (acc.length/2)){
                            for(e <- rep){
                                e!("decision", new Proposal(pv.s_num, pv.command))
                                // hmmmm this is going to look junky as output
                                // could use the symbol instead, it's in l_replicas somehere
                                println("!!!!!!!!!I'm leader " + l.id+ " I send decision with slot_num "+pv.s_num)
                            }
                            exit()
                        }//end if
                    }
                    else{
                        l!("Sorry", b)
                        exit()
                    }
                }//end case
            }//end receive
        }//end while
    }//end actt
}//end commander
