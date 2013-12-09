package multipaxos
import scala.io.Source
import scala.actors._
import scala.actors.Actor._
import scala.concurrent._

import paxutil._
// 'incR 
// 'incG 
// 'incB 
// 'decR 
// 'decG 
// 'decB 


object multipaxos extends App {
    val bstraps = List("local1.csv", "local2.csv", "local3.csv", "local4.csv", "local5.csv").map(fname => new Bootstrapper(fname))
    val initstate = new DemoData(new Colour(0, 0, 0))

    val servers = bstraps.map(bs => new Server(bs, initstate))

    val c1 = CommandFactory.makeCommand(1, 'incR)
    val c2 = CommandFactory.makeCommand(2, 'incR)
    val c3 = CommandFactory.makeCommand(3, 'incR )
    val c4 = CommandFactory.makeCommand(4, 'incR )
    val c5 = CommandFactory.makeCommand(5, 'incR  )
    val c6 = CommandFactory.makeCommand(6, 'incR  )
    val c7 = CommandFactory.makeCommand(7, 'incR )
    val c8 = CommandFactory.makeCommand(8, 'incR  )
    val c9 = CommandFactory.makeCommand(9, 'incR )
    servers.foreach(_.start)                  

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
    //servers(4).leader!("exit!")
    
    Thread.sleep(1000)


     for(s <- servers){
        
        s!("request", c6)
        s!("request", c7)
    }
   
   Thread.sleep(4000)

   for (s <- servers) {
        s!("request", c8)
        s!("request", c9)
   }

   Thread.sleep(4000)

   for(s <- servers){
       s.printArray()
   }
}
