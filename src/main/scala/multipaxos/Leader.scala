package multipaxos
import scala.actors._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor.{alive, register}

import paxutil._

class Leader(params : ActorData, localReplica : Replica, ls : ActorBag, rs : ActorBag, as : ActorBag)  extends Actor{
    // this is all (other) leaders
    val leaders = ls
    // all replicas
    val replicas = rs
    // all acceptors, local + remote
    val acceptors = as
    val port = params.port
    val id = params.id
    val pingtimeout = 500

    var leader_b_num = new B_num(0, id)
    var active = false
    var leader_proposals = new ProposalList(List[Proposal]())
    var commandercount = 0
    var scoutcount = 0
    var waitTime = 100

    def makeScout(ballot : B_num, slot_num : Int) = {
        new Scout(new ActorData(params.host, port, Symbol(id.name + "s" + scoutcount)),
                  this,
                  as,
                  ballot,
                  slot_num).start
        scoutcount += 1
    }

    def makeCommander(pv : Pvalue) = {
        new Commander(new ActorData(params.host, port, Symbol(params.id.name + "c" + commandercount)),
                      this,
                      rs, 
                      as,
                      pv).start
        commandercount = commandercount + 1
    }

    def getLeader(l_id : Symbol) : AbstractActor = leaders.getActBySym(l_id)

    def Leaderfun(l_acceptors : ActorBag, l_replicas : ActorBag) = {
        var acc = l_acceptors
        var rep = l_replicas

        receive{
            case (rep_id : Symbol, "propose", p : Proposal) => {
                if(!leader_proposals.exist_s_by_p(p)){
                    leader_proposals.put(p)
                    if(active){ 
                        makeCommander(new Pvalue(leader_b_num, p.s_num, p.command))
                    }
                }//end if
            }//end case

            case ("adopted", b : B_num, pvals : PvalueList) => {
                leader_proposals = leader_proposals.Xor(pvals.Pmax())
                println(id + ": ADOPTED, proposals to send:")
                leader_proposals.print()

                for(e <- leader_proposals.prlist){
                    makeCommander(new Pvalue(leader_b_num, e.s_num, e.command))
                }// end for
                active = true
                // reset wait time
                waitTime = 100
            }//end case
            case ("Sorry", b1: B_num) => {
                // I think this test is pointless
                if(b1 > leader_b_num) { 
                    active = false
                    println(id +": PRE-EMPTED by " + b1.getLeader + ", ballot was: " + b1 + ", my ballot is: " + leader_b_num + ", comparison result is : " + (b1 > leader_b_num) + ", I am active: " + active)
                    waitTime += 100
                    Thread.sleep(waitTime)
                    leader_b_num = new B_num(leader_b_num.b_num + 1, id)
                    // this is very bad, but hopefully it doesn't matter if its out of date
                    // still might get a piece of the integer...
                    makeScout(leader_b_num, localReplica.slot_num)
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
        val ss_num = localReplica.slot_num
        makeScout(leader_b_num, ss_num)

        println("I'm leader " + id + " and I started my scout")
        while(true){
             Leaderfun(acceptors, replicas)
        }
    }
}
