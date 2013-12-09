package multipaxos

import scala.io.Source
import scala.actors._
import scala.actors.Actor._
import scala.concurrent._

import paxutil._

//this class is used in Leader for concurrency

class Leader_Commander(l : Leader, l_acceptors : ActorBag, l_replicas : ActorBag, pv : Pvalue) extends Actor {
    var waitfor = l_acceptors.symbolsToList
    val acc = l_acceptors.actorsToList
    val rep = l_replicas.actorsToList
    for(s <- acc){
        // TODO
        s!("accept request", l, pv, this)
        //Console.println("As leader server: " + l.name + " in command I send accept reuest to " + s.name +" with pvalue:"+pv.toString())
    }

    def act(){
        while(true){
            receive{
                case ("accept reply", acc_id : Symbol, b : B_num) => {
                    //println("I'm leader, I got one accept reply from acceptors: "+s.name)
                    if(b.equal(pv.get_B_num())){
                        waitfor = waitfor diff List(acc_id)
                        //println("commander's waitfor length: "+ waitfor.length)
                        if(waitfor.length < (acc.length/2)){
                            for(e <- rep){
                                e!("decision", new Proposal(pv.s_num, pv.command))
                                // hmmmm this is going to look junky as output
                                // could use the symbol instead, it's in l_replicas somehere
                                println("I'm leader " + l.id+ " I send decision to replica server: " + e)
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
