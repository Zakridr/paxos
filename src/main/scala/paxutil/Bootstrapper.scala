package paxutil

import scala.io.Source

/* Structure for csv file:
 * ip address - String
 * port - Int
 * role - Symbol - either Replica, Server, Leader, or Acceptor
 * actor name - Symbol 
 */


class Bootstrapper(localnamesfile : String) {
    val allplayers = "allnodes.csv"
    val localplayers = localnamesfile

    val localdata = Source.fromFile(localplayers).getLines().toList
    val alldata = Source.fromFile(allplayers).getLines().toList

    // return value looks like:
    // (list of servers, list of replicas, list of leaders, list of acceptors)
    def processReadList(inputList : List[String]) :  
                  (List[ActorData],
                   List[ActorData],
                   List[ActorData],
                   List[ActorData])
                   = {
        val listtuples = inputList.map(_.split(',')).map( a => 
                (a(0), 
                 a(1).trim.toInt,
                 Symbol(a(2).trim),
                 Symbol(a(3).trim)))
        val servers = listtuples.filter(_._3 == 'Server).map(tup => new ActorData(tup._1, tup._2, tup._4))
        val replicas = listtuples.filter(_._3 == 'Replica).map(tup => new ActorData(tup._1, tup._2, tup._4))
        val acceptors = listtuples.filter(_._3 == 'Acceptor).map(tup => new ActorData(tup._1, tup._2, tup._4))
        val leaders = listtuples.filter(_._3 == 'Leader).map(tup => new ActorData(tup._1, tup._2, tup._4))
        return ( (servers, replicas, leaders, acceptors) )
    }

    def getParams4Remotes() = 
        processReadList(alldata diff localdata)
    def getParams4Local() = 
        processReadList(localdata)
}

// test code for reader class
object Test extends App {
    val bstrp1 = new Bootstrapper("local1.csv")
    val bstrp2 = new Bootstrapper("local2.csv")
    val bstrp3 = new Bootstrapper("local3.csv")
    val bstrp4 = new Bootstrapper("local4.csv")
    val bstrp5 = new Bootstrapper("local5.csv")
    bstrp1.getParams4Remotes._2.foreach(ad => println("data is: " + ad.host + " " + ad.port+ " " + ad.id))
    bstrp2.getParams4Remotes._2.foreach(ad => println("data is: " + ad.host + " " + ad.port+ " " + ad.id))
    bstrp3.getParams4Remotes._2.foreach(ad => println("data is: " + ad.host + " " + ad.port+ " " + ad.id))
    bstrp4.getParams4Remotes._2.foreach(ad => println("data is: " + ad.host + " " + ad.port+ " " + ad.id))
    bstrp5.getParams4Remotes._2.foreach(ad => println("data is: " + ad.host + " " + ad.port+ " " + ad.id))
    /*
    println(bstrp1.getParams4Remotes())
    println("")
    println(bstrp2.getParams4Remotes())
    println("")
    println(bstrp3.getParams4Remotes())
    println("")
    println(bstrp4.getParams4Remotes())
    println("")
    println(bstrp5.getParams4Remotes())
    println("")
    */
}
