package multipaxos
import scala.io.Source
import scala.actors._
import scala.actors.Actor._
import scala.concurrent._

//this class is used in Leader for concurrency

 class Leader_Commander(l:Leader, l_acceptors:List[Acceptor], l_replicas:List[Replica], pv:Pvalue) extends Actor{
        var waitfor = l_acceptors 
        var acc = l_acceptors
        var rep = l_replicas 
        for(s <- acc){
            s!("accept request", l, pv, this)
            //Console.println("As leader server: " + l.name + " in command I send accept reuest to " + s.name +" with pvalue:"+pv.toString())
        }

        def act(){            
            while(true){
                receive{
                    case ("accept reply", s:Acceptor, b:B_num) => {                       
                        //println("I'm leader, I got one accept reply from acceptors: "+s.name)
                        if(b.equal(pv.get_B_num())){
                            waitfor = waitfor diff List(s)
                            //println("commander's waitfor length: "+ waitfor.length)
                            if(waitfor.length < (acc.length/2)){
                                for(e <- rep){
                                    e!("decision", new Proposal(pv.s_num, pv.command))
                                    println("I'm leader " + l.name+ " I send decision to replica server: " + e.name)
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

