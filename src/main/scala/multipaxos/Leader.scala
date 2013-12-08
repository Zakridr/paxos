package multipaxos
import scala.io.Source
import scala.actors._
import scala.actors.Actor._
import scala.concurrent._

class Leader(sname: String) extends Actor{
    val name = sname
    var leader_id = -1
    var leaders = List[Leader]()
    var replicas = List[Replica]()
    var acceptors = List[Acceptor]()
    var leader_b_num = new B_num(0, -1)
    var active = false
    var leader_proposals = new ProposalList(List[Proposal]())
    //var scout_waitfor = servers diff List(this)

    def init(initr: List[Replica], inita:List[Acceptor], initl:List[Leader],l_id:Int) = {
      replicas = initr
      acceptors = inita
      leaders = initl
      leader_id = l_id
      leader_b_num = new B_num(0,l_id)
      //scout_waitfor = servers diff List(this)

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

    def Leaderfun(l_acceptors: List[Acceptor], l_replicas: List[Replica])={
        var acc = l_acceptors
        var rep = l_replicas

        
            receive{
                case (ss:Replica, "propose", p: Proposal)=>{
                    //Console.println("As leader server: " + name + " I receive a proposal:" + p.toString + " from Replica" + ss.name)
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
                    //Console.println("As leader server: " + name + " I got adoptted")
                    Console.println("As leader server: " + name + " I got adoptted and here is my proposals to be command: ")
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
                        println("I'm leader " + name +" I got preempt msg and start to ping active leader " + active_leader.name)
                        active = false
                        //now I start ping the current active leader until it is unavailable
                        
                        new ping(this, active_leader, 300).start

                    }

                }
                case ("scout")=>{
                    if(!active){
                        leader_b_num = new B_num(leader_b_num.b_num+1, leader_id)
                        val ss_num= replicas(leader_id).slot_num
                        new Leader_Scout(this, acc, leader_b_num,ss_num).start
                        Console.println("As leader server: " + name + " in Leaderfun I scout with b_num:" + leader_b_num.toString())

                    }

                }
                case ("ping!", x:Leader) =>{
                    if(active){
                        println("I'm active leader "+ name +" I send alive msg to leader "+ x.name)
                        x!("alive!")
                    }

                }
                //to simulate the case when the active leader died
                case ("exit!")=>{                  
                    exit()
                }

            }//end receive

        

    }//end Leaderfun


   
   
    def act(){
        //share the slot_num from its co-located replica
        val ss_num= replicas(leader_id).slot_num
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




