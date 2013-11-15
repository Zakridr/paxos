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

	// get the majority(top k server): assign a random number for each server and get the top k as majority
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
                var temp = List[(String, String, Int)]()
               	for(s <- (majority(servers,4) diff List(self))) { 
               		val future_reply = s !! ("prepare request", num+1)              	
               		future_reply() match{
               			case ("reply prepare request", leadername:String, n:Int) => temp = temp :+ future_reply               			
               		}
               	}//end for send propse to majority
                if(temp.size >= 3){
                	leader = temp.sortBy(a=>a._3).last._2
                	if(leader =="null") leader = name
                	for(s <- (majority(servers,4) diff List(self))){
                		s !("accept request", leader)
                	}
                }


               }//end if propose

			}//toss the coin and send msg to the majority
			 receive {
			 	case ("prepare request", number:Int) =>
			 	   if (leader == "none" && number > num){	
			 	       num = number       
			 	       reply(("reply prepare request", leadername, num))
			 	       //Console.println("server "+name+" accept " + leader +" as leader")
			 	   }
			 	case ("accept request", leadername:String){
			 		 if(leader == "none"){
			 		 	leader  = leadername
			 		 	Console.println("server "+name+" accept " + leader +" as leader")
			 		 }
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







