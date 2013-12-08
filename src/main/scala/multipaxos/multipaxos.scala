package multipaxos
import scala.io.Source
import scala.actors._
import scala.actors.Actor._
import scala.concurrent._
import scala.util.control.Breaks._

import paxutil.Bootstrapper

object multipaxos extends App{
    val bstraps = List("local1.csv", "local2.csv", "local3.csv", "local4.csv", "local5.csv").map(fname => new Bootstrapper(fname))

    val primeLeader = bstraps.head.getParams4Local._3.head
    println(primeLeader.id)

    val servers = bstraps.map(bs => new Server(primeLeader, bs))

    val c1 = new Command(1, 0, "write0")
    val c2 = new Command(1, 1, "write1")
    val c3 = new Command(1, 2, "write2")

    servers.foreach(_.start)

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
