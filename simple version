import scala.io.Source
import scala.actors._
import scala.actors.Actor._



class Server(name: String) extends Actor{
	var leader = "none"
	var servers = List[Server]()
	var num = -1
	//init servers 
	def init_servers(inits: List[Server])={
		servers = inits

	}

	// get the majority(top k server)
	def majority(servers: List[Server], k: Int):List[Server]={
		var majority_set = List[(Server, Int)]()
		//assign a random number for each of server
		val rand = new scala.util.Random
		for(s <- servers){
			majority_set = majority_set :+ (s, rand.nextInt())
		}
		majority_set = majority_set.sortBy(a=>a._2)
		majority_set = majority_set.take(k)
		return majority_set.unzip._1


	}

	def act(){
		
		while(true){
			if(leader == "none" ){  
			// toss the coin to decide if I want to send a prepare request  
			 val rand = new scala.util.Random           
                if(rand.nextInt(10) > 5){
                 Console.println("server "+name+" propose ")
               	for(s <- (majority(servers,4) diff List(self))) { s ! ("election", name)}
               }

			}//toss the coin and send msg to the majority
			 receive {
			 	case ("election", leadername:String) =>
			 	   if (leader == "none" ){	
			 	       leader = leadername	 	       
			 	       //sender ! ("reply prepare request", leadername)
			 	       Console.println("server "+name+" accept " + leader +" as leader")
			 	   }else{
			 	   	 sender ! ("election", leadername)
			 	   }
			 	

			 	
			 	
			 }

		}
	}
	def printleader(){
		Console.println("I'm Server " + name + "I accept " + leader + "as my leader")
	}

}

object election extends App{
	val  s1  = new Server("s1")
	val  s2  = new Server("s2")
	val  s3  = new Server("s3")
	val  s4  = new Server("s4")
	val  s5  = new Server("s5")
	s1.printleader
	val servers = List(s5,s2,s3,s4,s1)
	for(s <- servers){
		s.init_servers(servers)
	}

	for(s <- servers){
		s.start
	}


}







