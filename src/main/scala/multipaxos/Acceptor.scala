package multipaxos
import scala.io.Source
import scala.actors._
import scala.actors.Actor._
import scala.concurrent._





class Acceptor(sname:String) extends Actor{

    val name = sname
    var servers = List[Server]()
    var acceptor_b_num = new B_num(-1, -1)
    var acceptor_accepted = new PvalueList()


    def init(inits: List[Server])={
        servers = inits

    }

    def prune(slot_num:Int):PvalueList={
        if(acceptor_accepted.size()==0){
            return acceptor_accepted
        }
        else{
            return acceptor_accepted.filter_By_s_num(slot_num)
        }

    }

    def Acceptor_fun(){
        // as acceptor
        receive{               
            case ("prepare request", l:Leader, b:B_num, x:Leader_Scout, slot_num:Int) =>{
                //println("I'm acceptor server:"+ name +" I got prepare request")
                if(b > acceptor_b_num){
                    acceptor_b_num = b
                }

                //prune the pvalues according to the slot_num
                val pruned_accepted = prune(slot_num)
                x!("prepare reply", this, acceptor_b_num, pruned_accepted)
                Console.println("As accecptor server: " + name + " reply prepare request with b_num:" + acceptor_b_num.toString())
                Console.println("And I attached my accepted pvalues:" )
                pruned_accepted.print()

            }//end case
            case ("accept request", l:Leader, p:Pvalue, x:Leader_Commander) =>{
                //println("server "+ name + " find a accept request match")
                if(p.get_B_num() >= acceptor_b_num){
                    acceptor_b_num = p.get_B_num()
                    acceptor_accepted.put(p)
                }
                //Note here if use reply or sender!(msg), the Leader_Commander will not able to receive this msg
                x!("accept reply", this, acceptor_b_num)
                //println("hello, hello, hello, I receive  accept request from " + sender)
                //Console.println("As accecptor server: " + name + " reply accept request with b_num:" + acceptor_b_num.toString())
            }// end case


        }//end receive
    }//end Acceptor_fun

    def act(){
        println("acceptor: " + name + " Started")
        while(true){
            Acceptor_fun()
            
        }//end while

    }//end act


}//end class








