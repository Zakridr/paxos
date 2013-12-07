package multipaxos
import scala.io.Source
import scala.actors._
import scala.actors.Actor._
import scala.concurrent._
import scala.util.control.Breaks._


object multipaxos extends App{
    val s1 = new Server("s1", 1)
    val s2 = new Server("s2", 1)
    val s3 = new Server("s3", 1)
    val s4 = new Server("s4", 1)
    val s5 = new Server("s5", 1)
    
    val servers = List(s1,s2,s3,s4)
    for(s <- servers){
        s.init_servers(servers)
    }
    val c1 = new Command(1, 0, "write0")
    val c2 = new Command(1, 1, "write1")
    val c3 = new Command(1, 2, "write2")

    for(s <- servers){
        s.start
    }

   //broadcast request
    for(s <- servers){
        s!("request", c1)
        s!("request", c2)
        s!("request", c3)
    }
    

    Thread.sleep(2000)

    for(s <- servers){
        if(!s.isLeader()){
            s.printArray()
        }
    }


}
