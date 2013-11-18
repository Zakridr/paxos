import paxos.Server

object election extends App{
  val  s1  = new Server("s1")
  val  s2  = new Server("s2")
  val  s3  = new Server("s3")
  val  s4  = new Server("s4")
  val  s5  = new Server("s5")
  //s1.printleader
  val servers = List(s5,s2,s3,s4,s1)
  for(s <- servers){
    s.init_servers(servers)
  }

  for(s <- servers){
    s.start
  }

  Thread.sleep(5000)
  for (s <- servers) {
    s.printleader()
  }
}
