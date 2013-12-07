package util

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

    // return value looks like:
    // (list of servers, list of replicas, list of leaders, list of acceptors)
    def getParams4Remotes() :  (List[(String, Int, Symbol, Symbol)],
                   List[(String, Int, Symbol, Symbol)],
                   List[(String, Int, Symbol, Symbol)],
                   List[(String, Int, Symbol, Symbol)])
                   = {
        val remotelines = Source.fromFile(allplayers).getLines().toList diff 
                          Source.fromFile(localplayers).getLines().toList
        val listtuples = remotelines.map( l => l.split(',')).map( a => 
                (a(0), 
                 a(1).trim.toInt,
                 Symbol(a(2).trim),
                 Symbol(a(3).trim)))
        val servers = listtuples.filter(x => x._3 == 'Server)
        val replicas = listtuples.filter(x => x._3 == 'Replica)
        val acceptors = listtuples.filter(x => x._3 == 'Acceptor)
        val leaders = listtuples.filter(x => x._3 == 'Leader)
        return ( (servers, replicas, leaders, acceptors) )
    }
}

// test code for reader class
/*
object Test extends App {
    val bstrp1 = new Bootstrapper("local1.csv")
    val bstrp2 = new Bootstrapper("local2.csv")
    val bstrp3 = new Bootstrapper("local3.csv")
    val bstrp4 = new Bootstrapper("local4.csv")
    val bstrp5 = new Bootstrapper("local5.csv")
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
}
*/
