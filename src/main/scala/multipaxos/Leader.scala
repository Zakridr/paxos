package multipaxos
import scala.actors._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor.{alive, register}

import paxutil._

class Leader(params : ActorData, ls : ActorBag, rs : ActorBag, as : ActorBag)  extends Actor{
    var leader_id = -1

    val leaders = ls
    val replicas = rs
    val acceptors = as
    val port = params.port
    val id = params.id

    //TODO needs to change, changed the second type for B_num
    var leader_b_num = new B_num(0, -1)
    var active = false
    var leader_proposals = new ProposalList(List[Proposal]())
    //var scout_waitfor = servers diff List(this)

//TODO what is init being used for now...?
    def init(l_id:Int) = {
      leader_id = l_id
      leader_b_num = new B_num(0,l_id)
    }

    def getReplica(replica_id:Int):Replica={
        return replicas(replica_id)
    }
    def getAcceptor(acceptor_id:Int):Acceptor={
        return acceptors(acceptor_id)
    }
    def getLeader(l_id:Int):Leader={
        return leaders(l_id)
    }

    def Leaderfun(l_acceptors: ActorBag, l_replicas: ActorBag) = {
        var acc = l_acceptors
        var rep = l_replicas

        
            receive{
                case (rep_id:Symbol, "propose", p: Proposal)=>{
                   //println("As leader server: " + id + " I receive a proposal:" + p.toString + " from Replica" + rep_id)
                    if(!leader_proposals.exist_cmd(p.command)){
                        //println("I put the request into my propsal and active is:" + active)
                        leader_proposals.put(p)
                        if(active){  
                            new Leader_Commander(this, acc, rep, new Pvalue(leader_b_num, p.s_num, p.command)).start                 
                            
                        }
                    }//end if
                }//end case

                case ("adopted", b: B_num, pvals:PvalueList) =>{
                    leader_proposals = leader_proposals.Xor(pvals.Pmax())
                    println("As leader server: " + id + " I got adoptted and here is my proposals to be command:")
                    leader_proposals.print()

                    for(e <- leader_proposals.prlist){
                        new Leader_Commander(this, acc, rep, new Pvalue(leader_b_num, e.s_num, e.command)).start
                        
                        //println("ok, returned from function command")

                    }// end for
                    active = true
                }//end case
                case ("Sorry", b1: B_num) => {
                    if(b1 > leader_b_num && active ){
                        //TODO 
                        // needs to change...
                        val active_leader = getLeader(b1.getLeader())
                        println("I'm leader " + name +" I got preempt msg and start to ping active leader " + active_leader.name)
                        active = false
                        //now I start ping the current active leader until it is unavailable
                        
                        new ping(this, active_leader, 300).start
                    }
                }
                case ("scout")=>{
                    if(!active){
                        leader_b_num = new B_num(leader_b_num.b_num+1, leader_id)
                        // TODO this is totally broken
                        val ss_num= replicas(leader_id).slot_num
                        new Leader_Scout(this, acc, leader_b_num,ss_num).start
                        Console.println("As leader server: " + name + " in Leaderfun I scout with b_num:" + leader_b_num.toString())

                    }
                }
                case ("ping!", remote_leader_sym : Symbol) =>{
                    if(active){
                        //TODO broken...
                        println("I'm active leader "+ name +" I send alive msg to leader "+ remote_leader_sym)
                        sender!("alive!")
                    }

                }
                //to simulate the case when the active leader died
                case ("exit!")=>{                  
                    exit()
                }

            }//end receive

        

    }//end Leaderfun

    def act(){
        alive(port)
        register(id, self)

        //share the slot_num from its co-located replica
        val ss_num= replicas(leader_id).slot_num
        // TODO needs to be changed
        new Leader_Scout(this, acceptors, leader_b_num, ss_num).start

        while(true){
             Leaderfun(acceptors, replicas)
        }
    }
}

// this actor(thread) send synchronize msg to the active leader, if no response within mils seconds,scout and exit
class ping(me:Leader, active_leader:Leader, mils: Int) extends Actor{
    def act(){
        while(true){
            val result = active_leader !?(mils, ("ping!", me))
            println("I'm leader " + me.name +" I got ping result: " + result)
            if(result== None){
                println("I'm leader "+me.name+" I should scout")
                me!("scout")
                exit()
            }
        }
    }
}
