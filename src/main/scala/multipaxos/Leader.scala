package multipaxos
import scala.actors._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor.{alive, register}

import paxutil._

class Leader(params : ActorData, localReplica : Replica, ls : ActorBag, rs : ActorBag, as : ActorBag)  extends Actor{

    val leaders = ls
    val replicas = rs
    val acceptors = as
    val port = params.port
    val id = params.id

    val pingtimeout = 300

    var leader_b_num = new B_num(0, id)
    var active = false
    var leader_proposals = new ProposalList(List[Proposal]())
    //var scout_waitfor = servers diff List(this)

    /*
    def getReplica(replica_id:Int):Replica={
        return replicas(replica_id)
    }
    def getAcceptor(acceptor_id:Int):Acceptor={
        return acceptors(acceptor_id)
    }
    */
    def getLeader(l_id : Symbol) : AbstractActor = leaders.getActBySym(l_id)

    def Leaderfun(l_acceptors : ActorBag, l_replicas : ActorBag) = {
        var acc = l_acceptors
        var rep = l_replicas

        receive{
            case (rep_id : Symbol, "propose", p : Proposal) => {
                //println("As leader server: " + id + " I receive a proposal:" + p.toString + " from Replica" + rep_id)
                if(!leader_proposals.exist_cmd(p.command)){
                    //println("I put the request into my propsal and active is:" + active)
                    leader_proposals.put(p)
                    if(active){  
                        new Leader_Commander(this, acc, rep, new Pvalue(leader_b_num, p.s_num, p.command)).start
                    }
                }//end if
            }//end case

            case ("adopted", b : B_num, pvals : PvalueList) => {
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
                    val active_leader = getLeader(b1.getLeader())
                    println("I'm leader " + id +" I got preempt msg and start to ping active leader " + b1.getLeader)
                    active = false
                    //now I start ping the current active leader until it is unavailable

                    new Ping(this, active_leader, pingtimeout).start
                }
            }
            case ("scout")=>{
                if(!active){
                    leader_b_num = new B_num(leader_b_num.b_num+1, id)
                    // TODO 
                    // hmmm
                    val ss_num= localReplica.slot_num
                    new Leader_Scout(this, params, acc, leader_b_num,ss_num).start
                    Console.println("As leader server: " + id + " in Leaderfun I scout with b_num:" + leader_b_num.toString())
                }
            }
            case ("ping!", remote_leader_sym : Symbol) => {
                if(active){
                    // TODO using sender here
                    println("I'm active leader "+ id +" I send alive msg to leader "+ remote_leader_sym)
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
        // TODO
        println("I'm leader " + id + " and I'm sending prepare request")
        val ss_num= localReplica.slot_num
        new Leader_Scout(this, params, acceptors, leader_b_num, ss_num).start

        println("I'm leader " + id + " and I started my scout")
        while(true){
             Leaderfun(acceptors, replicas)
        }
    }
}

// this actor(thread) send synchronize msg to the active leader, if no response within mils seconds,scout and exit
class Ping(me : Leader, active_leader : AbstractActor, mils : Int) extends Actor{
    def act(){
        while(true){
            val result = active_leader !? (mils, ("ping!", me.id))
            println("I'm leader " + me.id +" I got ping result: " + result)
            if(result== None){
                println("I'm leader "+me.id+" I should scout")
                me!("scout")
                exit()
            }
        }
    }
}
