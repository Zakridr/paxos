package multipaxos
import scala.io.Source
import scala.actors._
import scala.actors.Actor._
import scala.concurrent._

 //leader function scout
    class Leader_Scout(l:Leader, l_acceptors:List[Acceptor], b:B_num, slot_num: Int) extends Actor{
        var acc = l_acceptors 
        var scout_waitfor = acc
        var pvalues = new PvalueList()
        for(s <- acc){
            s!("prepare request", l, b, this, slot_num)
            //Console.println("As scout leader server: " + l.name + " I send prepare request to acceptor: " + s.name +" with b_num:"+b.toString())
        }
        def act(){
        while(true){
            receive{
                case ("prepare reply", s:Acceptor, b1:B_num, r:PvalueList) =>{
                    //Console.println("As scout leader server: " + l.name + " I got repare request from" + s.name+ " with its accepted pvaluelist:")
                    //r.print()
                    if(b.equal(b1)){
                        pvalues.putList(r)
                        scout_waitfor = scout_waitfor diff List(s)
                        ////println("now waitfor length is: "+ scout_waitfor.length + " acceptors length/2 is: "+ acc.length/2)
                        if(scout_waitfor.length < (acc.length/2)){
                            //println("I'm leader, in Scout, I send adopted")
                            l!("adopted", b, pvalues)
                            exit()
                        }
                    }
                    else{
                        l!("Sorry", b1)
                        exit()
                    }
                }//end case
            }//end receive
        }// end while
           
      }

    }//end Scout
