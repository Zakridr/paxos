package multipaxos
import scala.io.Source
import scala.actors._
import scala.actors.Actor._
import scala.concurrent._
import scala.util.control.Breaks._



class Leader(sname: String, l_id:Int) extends Actor{
    val name = sname
    val leader_id = l_id
    var replicas = List[Replica]()
    var acceptors = List[Acceptor]()
    var leader_b_num = new B_num(0, leader_id)
    var active = false
    var leader_proposals = new ProposalList(List[Proposal]())
    //var scout_waitfor = servers diff List(this)

    def init(initr: List[Replica], inita:List[Acceptor]) = {
      replicas = initr
      acceptors = inita
      //scout_waitfor = servers diff List(this)

    }

    def getReplica(replica_id:Int):Replica={
        return replicas(replica_id)
    }
    def getAcceptor(acceptor_id:Int):Acceptor={
        return acceptors(acceptor_id)
    }

    def Leaderfun(l_acceptors: List[Acceptor], l_replicas: List[Replica])={
        var acc = l_acceptors
        var rep = l_replicas
        
        //while(true){
            receive{
                case (ss:Replica, "propose", p: Proposal)=>{
                    Console.println("As leader server: " + name + " I receive a proposal:" + p.toString + " from Replica" + ss.name)
                    if(!leader_proposals.exist_cmd(p.command)){
                        println("I put the request into my propsal and active is:" + active)
                        leader_proposals.put(p)
                        if(active){var returnB = Commander(this, acc, rep, new Pvalue(leader_b_num, p.s_num, p.command))}
                    }//end if
                }//end case

                case ("adopted", b: B_num, pvals:PvalueList) =>{
                    leader_proposals = leader_proposals.Xor(pvals.Pmax())
                    Console.println("As leader server: " + name + " I got adoptted and here is my proposals to be command:")
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
                        Console.println("As leader server: " + name + " in Leaderfun I scout with b_num:" + leader_b_num.toString())
                    }

                }
            }//end receive

        //}//end while

    }

    //leader function scout
    def Scout(l:Leader, l_acceptors:List[Acceptor], b:B_num):Boolean={
        var acc = l_acceptors 
        var scout_waitfor = acc
        var pvalues = new PvalueList()
        for(s <- acc){
            s!("prepare request", this, b)
            Console.println("As leader server: " + name + " I send prepare request to acceptor: " + s.name +" with b_num:"+b.toString())
        }
        while(true){
            receive{
                case ("prepare reply", s:Acceptor, b1:B_num, r:PvalueList) =>{
                    Console.println("As leader server: " + name + " I got repare request from" + s.name+ " with its accepted pvaluelist:")
                    r.print()
                    if(b.equal(b1)){
                        pvalues.putList(r)
                        scout_waitfor = scout_waitfor diff List(s)
                        println("now waitfor length is: "+ scout_waitfor.length + " acceptors length/2 is: "+ acc.length/2)
                        if(scout_waitfor.length < (acc.length/2)){
                            println("I'm leader, in Scout, I send adopted")
                            l!("adopted", b, pvalues)
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

   

    def Commander(l:Leader, l_acceptors:List[Acceptor], l_replicas:List[Replica], pv:Pvalue):Boolean={
        var waitfor = l_acceptors 
        var acc = l_acceptors
        var rep = l_replicas 
        for(s <- acc){
            s!("accept request", this, pv)
            Console.println("As leader server: " + name + " in command I send accept reuest to " + s.name +" with pvalue:"+pv.toString())
        }
       while(true){
            receive{
                case ("accept reply", s:Acceptor, b:B_num) => {
                    println("I'm leader, I got one accept reply from acceptors: "+s.name)
                    if(b.equal(pv.get_B_num())){
                        waitfor = waitfor diff List(s)
                        println("commander's waitfor length: "+ waitfor.length)
                        if(waitfor.length < (acc.length/2)){
                            for(e <- rep){
                                e!("decision", new Proposal(pv.s_num, pv.command))
                                println("I'm leader, I send decision to server: " + e.name)
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
        var returnA= Scout(this, acceptors, leader_b_num)
        while(true){
             Leaderfun(acceptors, replicas)
        }

    }
   

}