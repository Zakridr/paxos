import scala.io.Source
import scala.actors._
import scala.actors.Actor._
import scala.concurrent._



class Server(name: String) extends Actor{
	var leader = "none"
	var servers = List[Server]()
	var num = -1//track the highest current propose number in the network
	var selfnum = -1//record the propose times 
	var propose_msg_num = 0// record the number of majority msgs sent
	var msg_box = List[(String, Int)]()// save all the reply of "prepare request", the proposer can then choose the highest num for value
	var sent = 0//flag for proposer so that it will not propose multiple round

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
			if(leader == "none" && sent==0){  
			// toss the coin to decide if I want to send a prepare request  
			 val rand = new scala.util.Random           
                if(rand.nextInt(10) > 3){
               	for(s <- (majority(servers,4) diff List(self))) { 
               		Console.println("server "+name+" propose "+ "to sb with number "+ (selfnum+1))
               		s ! ("prepare request", selfnum+1)  
               		propose_msg_num = propose_msg_num +1            	
               		
               	}//end for send propse to majority
                sent = 1
               }//end if propose

			}//toss the coin and send msg to the majority
			 receive {
			 	case ("prepare request", number:Int) =>
			 	   if (leader == "none" && number > num){	
			 	       num = number       
			 	       sender ! ("reply prepare request", leader, num)
			 	       Console.println("server "+name+" accept prepare request number"+ num)
			 	   }
			 	case ("reply prepare request", leadername:String, number:Int)=>{
			 		if(propose_msg_num > 0){
			 			propose_msg_num = propose_msg_num -1
			 			msg_box = msg_box :+ (leadername, number)

			 			if(propose_msg_num==0){
			 				msg_box = msg_box.sortBy(a=>a._2)
			 				leader = msg_box.last._1
			 				num = msg_box.last._2
			 				if(leader =="none")
			 				   leader = name
			 				for(s <- (majority(servers,4) diff List(self))){
                				s !("accept request", leader, num)
                			}
                			//clear
                			msg_box = List[(String, Int)]()//clear msg_box
                			sent = 0
                			selfnum = num

			 			}
			 		}
			 		

			 	}
			 	case ("accept request", leadername:String, number:Int)=>{
			 		 if(leader == "none" && number>=num){
			 		 	leader  = leadername
			 		 	Console.println("server "+name+" accept " + leader +" as leader")
			 		 }
			 	}
			 	

			 	
			 	
			 }

		}
	}
	def printleader(){
		Console.println("I'm Server " + name + " I accept " + leader + " as my leader")
	}

}

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


   Thread.sleep(5000);
   for(s <- servers){
		s.printleader()
	}

}







