package multipaxos
import scala.actors._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor.{alive, register}

import paxutil._


class Leader(pLeader : ActorData, params : ActorData, rs : ActorBag, as : ActorBag)  extends Actor{
    val leader_id = pLeader.id
    val replicas = rs
    val acceptors = as
    val port = params.port
    val id = params.id

    var leader_b_num = new B_num(0, leader_id)
    var active = false
    var leader_proposals = new ProposalList(List[Proposal]())
    //var scout_waitfor = servers diff List(this)

    def Leaderfun(acc: ActorBag, rep: ActorBag)={
        
        //while(true){
            receive{
                case (rep_id:Symbol, "propose", p: Proposal)=>{
                    println("As leader server: " + id + " I receive a proposal:" + p.toString + " from Replica" + rep_id)
                    if(!leader_proposals.exist_cmd(p.command)){
                        println("I put the request into my propsal and active is:" + active)
                        leader_proposals.put(p)
                        if(active){var returnB = Commander(this, acc, rep, new Pvalue(leader_b_num, p.s_num, p.command))}
                    }//end if
                }//end case

                case ("adopted", b: B_num, pvals:PvalueList) =>{
                    leader_proposals = leader_proposals.Xor(pvals.Pmax())
                    println("As leader server: " + id + " I got adoptted and here is my proposals to be command:")
                    leader_proposals.print()
                    for(e <- leader_proposals.prlist){
                        var returnB = Commander(this, acc, rep, new Pvalue(leader_b_num, e.s_num, e.command))
                        println("ok, returned from function command")

                    }// end for
                    active = true
                }//end case
                case ("Sorry", b1: B_num) => {
                    if(b1 > leader_b_num){
                        active = false
                        leader_b_num = new B_num(leader_b_num.b_num+1, leader_id)
                        //scout_waitfor = acc diff List(this)
                        var returnA = Scout(this, acc, leader_b_num)
                        println("As leader server: " + id + " in Leaderfun I scout with b_num:" + leader_b_num.toString())
                    }

                }
            }//end receive

        //}//end while

    }

    //leader function scout
    def Scout(l:Leader, l_acceptors:ActorBag, b:B_num):Boolean={
        val acc = l_acceptors.actorsToList
        var scout_waitfor = l_acceptors.symbolsToList
        var pvalues = new PvalueList()
        for(s <- acc){
            s!("prepare request", id, b)
            println("As leader server: " + id + " I send prepare request to acceptor: " + s ) // +" with b_num:"+b.toString())
        }

        while(true){
            receive{
                case ("prepare reply", acc_id : Symbol, b1:B_num, r:PvalueList) =>{
                    println("As leader server: " + id + " I got repare request from" + acc_id + " with its accepted pvaluelist:")
                    r.print()
                    if(b.equal(b1)){
                        pvalues.putList(r)
                        scout_waitfor = scout_waitfor diff List(acc_id)
                        println("now waitfor length is: "+ scout_waitfor.length + " acceptors length/2 is: "+ acc.length/2)
                        if(scout_waitfor.length < (acc.length/2)){
                            println("I'm leader, in Scout, I send adopted")
                            l ! ("adopted", b, pvalues)
                            return true
                        }
                    }
                    else{
                        l!("Sorry", b1)
                        return true
                    }
                }//end case
            }//end receive
        }// end while
           return true


    }//end Scout

   

    def Commander(l:Leader, l_acceptors:ActorBag, l_replicas:ActorBag, pv:Pvalue):Boolean={
        var waitfor = l_acceptors.symbolsToList
        var acc = l_acceptors.actorsToList
        var rep = l_replicas.actorsToList
        for(s <- acc){
            s!("accept request", id, pv)
            println("As leader server: " + id + " in command I send accept reuest to " + s +" with pvalue:"+pv.toString())
        }
       while(true){
            receive{
                case ("accept reply", acc_id:Symbol, b:B_num) => {
                    println("I'm leader, I got one accept reply from acceptors: "+acc_id)
                    if(b.equal(pv.get_B_num())){
                        waitfor = waitfor diff List(acc_id)
                        println("commander's waitfor length: "+ waitfor.length)
                        if(waitfor.length < (acc.length/2)){
                            for(e <- rep){
                                e!("decision", new Proposal(pv.s_num, pv.command))
                                println("I'm leader, I send decision to server: " + e)
                            }
                            return true
                        }//end if
                    }
                    else{
                        l!("Sorry", b)
                         return true
                        
                    }
                }//end case
            }//end receive
        }//end while
        return true

    }//end commander

    def act(){
        alive(port)
        register(id, self)

        var returnA= Scout(this, acceptors, leader_b_num)
        while(true){
             Leaderfun(acceptors, replicas)
        }
    }
}
