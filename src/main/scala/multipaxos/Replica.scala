package multipaxos
import scala.actors._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor.{alive, register}

import paxutil.ActorData

class Replica(pLeader : ActorData, params : ActorData, rLeaders : List[AbstractActor], lLeader : AbstractActor) extends Actor{
    val localLeader = lLeader
    val remoteLeaders = rLeaders
    val primeLeader = pLeader.makeRemoteActor
    val port = params.port
    val id = params.id

    var array_content = new Array[String](100)
    var state = -1
    var slot_num = 0
    var replicas_proposals = new ProposalList(List[Proposal]()) // empty initially
    var replicas_decisions = new ProposalList(List[Proposal]())//empty initially

    def min_s_num(s1: Set[Int], s2: Set[Int]):Int={ 
        val s3 = s1 | s2 //union of s_sum
        // new set range to smallest number of union set to largest number of union set
        if(s3.size>0){
            val s4 = (s3.min to (s3.max+1)).toSet 
            val s5  = s4 &~ s3
            return s5.min
        }
        return 0       
    }

    // replicas propose function
    def propose(c:Command)={
        if(!replicas_decisions.exist_cmd(c)){// if command has not yet been a decision
            val s_min = min_s_num(replicas_decisions.s_set, replicas_proposals.s_set)
            val temp_p = new Proposal(s_min, c)
            replicas_proposals.put(temp_p)
            // TODO, sending an actor...
            primeLeader ! (this, "propose", temp_p)
            println("As server " + id + " propose to leader with proposal: " + temp_p.toString)
        }
    }


    // replicas perfom function
    def perform(c:Command)={
        if(replicas_decisions.exist_cmd(c) && replicas_decisions.getBy_cmd(c).head.s_num < slot_num){
            slot_num += 1
        }else{
            array_content(slot_num) = c.getOp()
            state += 1
            slot_num += 1
            println("As server "+this.id + " update array_content(" + (slot_num-1) +") = " + c.getOp())
            //TODO 
            //send response to client
       }

    }


    def Replica_fun(){
        receive{
            case ("request", c: Command) => {
                    propose(c)
                    println("As replica server: " + id + " I got request" + c.toString())
                }
            case ("decision", p: Proposal) =>{

                println("As replica server: " + id + " I got decision" + p.toString())
                replicas_decisions.put(p)
                while(replicas_decisions.exist_s(slot_num)){
                    val temp1 = replicas_decisions.getBy_s(slot_num).head
                    if(replicas_proposals.exist_s(slot_num)){
                        val temp2 = replicas_proposals.getBy_s(slot_num).head
                        if(!temp1.equal(temp2)){
                            println("As replica server: " + id + " I found the a decision took slot" + slot_num+"and repropose"+temp2.toString())
                                 //I add this line so that the replicas proposal size is reasonable
                            replicas_proposals.remove(temp2)
                            propose(temp2.command)


                        }
                    }
                    perform(temp1.command)
                    println("As replica server: " + id + " perform decision" + p.toString())

                }//end while

            }//end case
        }//end receive

    }
     def printArray(){
        println("print array:")
        for(i <- 0 to slot_num-1){
            print(array_content(i) + " ")
        }
        println()

    }

    def act(){
        alive(port)
        register(id, self)
             
        while(true){
            Replica_fun()
            
        }//end while

    }//end act




}
