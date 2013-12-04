package multipaxos
import scala.io.Source
import scala.actors._
import scala.actors.Actor._
import scala.concurrent._
import scala.util.control.Breaks._




class Acceptor(sname:String, l_id:Int) extends Actor{

    val name = sname
    var leader_id = l_id
    var servers = List[Server]()
    var acceptor_b_num = new B_num(-1, leader_id)
    var acceptor_accepted = new PvalueList()

    def leader():Leader = {
        return servers(leader_id).leader
    }
    def init(inits: List[Server])={
        servers = inits

    }

    def Acceptor_fun(){
        // as acceptor
        receive{               
            case ("prepare request", l:Leader, b:B_num) =>{
                println("I'm server:"+ name +" I got prepare request")
                if(b > acceptor_b_num){
                    acceptor_b_num = b
                }
                leader()!("prepare reply", this, acceptor_b_num, acceptor_accepted)
                Console.println("As accecptor server: " + name + " reply prepare request with b_num:" + acceptor_b_num.toString())
                Console.println("And I attached my accepted pvalues:" )
                acceptor_accepted.print()

            }//end case
            case ("accept request", l:Leader, p:Pvalue) =>{
                println("server "+ name + " find a accept request match")
                if(p.get_B_num() >= acceptor_b_num){
                    acceptor_b_num = p.get_B_num()
                    acceptor_accepted.put(p)
                }
                leader()!("accept reply", this, acceptor_b_num)
                Console.println("As accecptor server: " + name + " reply accept request with b_num:" + acceptor_b_num.toString())
            }// end case

        }//end receive
    }//end Acceptor_fun

    def act(){
        println("acceptor: " + name + "Started")
        while(true){
            Acceptor_fun()
            
        }//end while

    }//end act


}//end class








