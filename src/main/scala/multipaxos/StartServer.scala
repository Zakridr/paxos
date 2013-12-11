package multipaxos
import paxutil._

object StartServer extends App {
    //val bstraps = List("local1.csv", "local2.csv", "local3.csv", "local4.csv", "local5.csv").map(fname => new Bootstrapper(fname))

    println(args(0))
    val initstate = new DemoData(new Colour(0, 0, 0))
    val bstrap = new Bootstrapper(args(0))

    val server = new Server(bstrap, initstate)
    server.start
}
