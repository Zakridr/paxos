package multipaxos
import scala.io.Source
import scala.actors._
import scala.actors.Actor._
import scala.concurrent._



object multipaxos extends App{
    val s1 = new Server("s1")
    val s2 = new Server("s2")
    val s3 = new Server("s3")
    val s4 = new Server("s4")
    val s5 = new Server("s5")
    val s6 = new Server("s6")
    val servers = List(s1,s2,s3,s4,s5,s6)

    for(s <- servers){
        s.init_servers(servers)
    }
    val c1 = new Command(1, 0, "write0")
    val c2 = new Command(1, 1, "write1")
    val c3 = new Command(1, 2, "write2")
    val c4 = new Command(1, 3, "write3")
    val c5 = new Command(1, 4, "write4")
    val c6 = new Command(1, 5, "write5")
    val c7 = new Command(1, 6, "write6")

    for(s <- servers){
        s.start
    }

   //broadcast request
    for(s <- servers){
        s!("request", c1)
        s!("request", c2)
        s!("request", c3)
    }

    // s4 has the biggest b_num, it is highly possible to be chosen as leader
    
    

     for(s <- servers){
        s!("request", c4)      
        s!("request", c5)
       

    }
    //because s6's the b_num is relatively high, so it is highly possible to be chosen as leader
    //we killed it here to see if somebody else took his place
    s6.leader!("exit!")
    
    Thread.sleep(1000)


     for(s <- servers){
        
        s!("request", c6)
        s!("request", c7)

    }



   
     Thread.sleep(2000)

    for(s <- servers){
        s.printArray()
        
    }

   


}
