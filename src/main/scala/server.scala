package paxos

import scala.actors._
import scala.actors.Actor._
import scala.concurrent._


class Server(sname: String, l_id: Int) extends Actor{
    // as replicas
    val name = sname
    var servers = List[Server]()
    var leader_id = l_id
    var array_content = new Array[String](100)
    var state = -1
    var slot_num = 0
    var replicas_proposals = new ProposalList(List[Proposal]()) // empty initially
    var replicas_decisions = new ProposalList(List[Proposal]())//empty initially

    // as Acceptor
    var acceptor_b_num = new B_num(-1, leader_id)
    var acceptor_accepted = new PvalueList()

    // as Leader
    var leader_b_num = new B_num(0, leader_id)
    var active = false
    var leader_proposals = new ProposalList(List[Proposal]())
    var scout_waitfor = servers diff List(this)


    def isLeader():Boolean = {
        if(leader().name==this.name) return true
        return false
    }
    def init_servers(inits: List[Server]) = {
      servers = inits
      scout_waitfor = servers diff List(this)

    }
    def leader():Server = {
        return servers(leader_id)
    }

    def getServer(server_id:Int):Server={
        return servers(server_id)
    }

        // get the smallest s_num of set s1 and s2
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
            leader() ! (this, "propose", temp_p)
            Console.println("as server "+this.name + " propose to leader with proposal: " + temp_p.toString)
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
            Console.println("As server "+this.name + " update array_content(" + (slot_num-1) +") = " + c.getOp())
            //TODO 
            //send response to client
       }

    }

    // leader function
    def Leaderfun(acceptors: List[Server], replicas: List[Server])={
        var acc = acceptors
        var rep = replicas
        println("now scout_waitfor length is: "+scout_waitfor.length)
        if(scout_waitfor.length > 0){
            Scout(this, servers, leader_b_num)
        }
        
        //while(true){
            receive{
                case (ss:Server, "propose", p: Proposal)=>{
                    Console.println("As leader server: " + name + " I receive a proposal:" + p.toString + " from " + ss.name)
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
                        scout_waitfor = acc diff List(this)
                        Scout(this, acc, leader_b_num)
                        Console.println("As leader server: " + name + " in Leaderfun I scout with b_num:" + leader_b_num.toString())
                    }

                }
            }//end receive

        //}//end while

    }

    //leader function scout
    def Scout(l:Server, acceptors:List[Server], b:B_num)={
        var acc = acceptors diff List(this)
        var pvalues = new PvalueList()
        for(s <- acc){
            s!("prepare request", this, b)
            Console.println("As leader server: " + name + " I send prepare request to:" + s.name +" with b_num:"+b.toString())
        }
        //while(true){
            receive{
                case ("prepare reply", s:Server, b1:B_num, r:PvalueList) =>{
                    Console.println("As leader server: " + name + " I got repare request from" + s.name+ " with its accepted pvaluelist:")
                    r.print()
                    if(b.equal(b1)){
                        pvalues.putList(r)
                        scout_waitfor = scout_waitfor diff List(s)
                        println("now waitfor length is: "+ scout_waitfor.length + " acceptors length/2 is: "+ acc.length/2)
                        if(scout_waitfor.length < (acc.length/2)){
                            println("I'm leader, in Scout, I send adopted")
                            l!("adopted", b, pvalues)
                            //break
                            //exit()?
                        }
                    }
                    else{
                        l!("Sorry", b1)
                        //break
                        //exit()
                    }
                }//end case
            }//end receive
        //}// end while


    }//end Scout

   

    def Commander(l:Server, acceptors:List[Server], replicas:List[Server], pv:Pvalue):Boolean={
        var waitfor = acceptors diff List(this)
        var acc = acceptors diff List(this)
        for(s <- acc){
            s!("accept request", this, pv)
            Console.println("As leader server: " + name + " in command I send accept reuest to " + s.name +" with pvalue:"+pv.toString())
        }
       while(true){
            receive{
                case ("accept reply", s:Server, b:B_num) => {
                    println("I'm leader, I got one accept reply from server: "+s.name)
                    if(b.equal(pv.get_B_num())){
                        waitfor = waitfor diff List(s)
                        println("commander's waitfor length: "+ waitfor.length)
                        if(waitfor.length < (acc.length/2)){
                            for(e <- replicas){
                                e!("decision", new Proposal(pv.s_num, pv.command))
                                println("I'm leader, I send decision to server: " + e.name)
                            }
                            return true
                        //exit()?
                        }//end if
                    }
                    else{
                        l!("Sorry", b)
                         return true
                        //break
                        //exit()
                    }
                }//end case
            }//end receive
        }//end while
        return true

    }//end commander

   

    def Acceptor_Replica_fun(){
        // as acceptor
        receive{               
            case ("prepare request", l:Server, b:B_num) =>{
                println("I'm server:"+ name +" I got prepare request")
                if(b > acceptor_b_num){
                    acceptor_b_num = b
                }
                leader()!("prepare reply", this, acceptor_b_num, acceptor_accepted)
                Console.println("As accecptor server: " + name + " reply prepare request with b_num:" + acceptor_b_num.toString())
                Console.println("And I attached my accepted pvalues:" )
                acceptor_accepted.print()

            }//end case
            case ("accept request", l:Server, p:Pvalue) =>{
                println("server "+ name + " find a accept request match")
                if(p.get_B_num() >= acceptor_b_num){
                    acceptor_b_num = p.get_B_num()
                    acceptor_accepted.put(p)
                }
                leader()!("accept reply", this, acceptor_b_num)
                Console.println("As accecptor server: " + name + " reply accept request with b_num:" + acceptor_b_num.toString())
            }// end case

             // as replicas
                case ("request", c: Command) => {
                    propose(c)
                    Console.println("As replica server: " + name + " I got request" + c.toString())
                }
                case ("decision", p: Proposal) =>{
                    Console.println("As replica server: " + name + " I got decision" + p.toString())
                    replicas_decisions.put(p)
                    while(replicas_decisions.exist_s(slot_num)){
                        val temp1 = replicas_decisions.getBy_s(slot_num).head
                        if(replicas_proposals.exist_s(slot_num)){
                            val temp2 = replicas_proposals.getBy_s(slot_num).head
                            if(!temp1.equal(temp2)){
                                Console.println("As replica server: " + name + " I found the a decision took slot" + slot_num+"and repropose"+temp2.toString())
                                 //I add this line so that the replicas proposal size is reasonable
                                replicas_proposals.remove(temp2)
                                propose(temp2.command)


                            }
                        }
                        perform(temp1.command)
                        Console.println("As replica server: " + name + " perform decision" + p.toString())

                    }//end while

                }//end case


        }//end receive
    }//end Acceptorfun


    def act(){
             
        while(true){
            if(isLeader()){
                println("leaderfun called")
                Leaderfun(servers, servers)
            }
            else{
                Acceptor_Replica_fun()
                
            }
            
        }//end while

    }//end act

    def printArray(){
        println("print array:")
        for(i <- 0 to slot_num-1){
            print(array_content(i) + " ")
        }
        println()

    }


}
