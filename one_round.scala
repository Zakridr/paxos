import scala.io.Source
import scala.actors._
import scala.actors.Actor._
import scala.concurrent._

class Proposal(p: Int, proposer_name : String) {
  val propnum = p
  val proposer = proposer_name

  var value = "blank"

  def <(that: Proposal): Boolean = {
    return (this.propnum < that.propnum || 
      (this.propnum == that.propnum && this.proposer < that.proposer))
  }

  def <=(that: Proposal): Boolean = {
    return (this.propnum < that.propnum || 
      (this.propnum == that.propnum && this.proposer <= that.proposer))
  }

  def ==(that: Proposal) : Boolean = {
    return (this.propnum == that.propnum && this.proposer == that.proposer && this.value == that.value)
  }

  def setValue(v : String) = {
    value = v
  }

  def getValue() : String = {
    return value
  }

  def hasValue() : Boolean = {
    return value != "blank"
  }
}

class Server(n: String) extends Actor{
  var leader = "none"
  var servers = List[Server]()

  // my new stuff
  val rnumgen = new scala.util.Random
  val name = n
  val timeout = 500 //timeout for all messages

  var acceptedproposal = new Proposal(-1, name)
  var biggestrecved = acceptedproposal // what type for values?
  var lastoffer = 0


  //init servers 
  def init_servers(inits: List[Server]) = {
    servers = inits
  }

  // get the majority(top k server): assign a random number for each server and get the top k as majority
  def majority(servers: List[Server], k: Int): List[Server] = {
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

  def flipcoin() : Boolean = {
    return rnumgen.nextInt(servers.length) == 0
  }

  def proposerphase1() = {
    // generate new proposal number
    // get majority
    // send messages, go to next phase
    //val acceptors = 

    // can change this in future to include all servers, or whatever
    val quorum = majority(servers diff List(this), servers.length / 2 + 1)
    for (acceptor <- quorum) {
      Console.println("server "+name+": " + acceptor.name +" is in my quorum")
    }
    val proposal = new Proposal(lastoffer + 1, name)
    for (s <- quorum ) {
      s ! ("prepare request", proposal)
    }
    lastoffer += 1

    proposerphase2(quorum, quorum, proposal, new Proposal(-1, ""))
  }

  def proposerphase2(quorum : List[Server], 
                     unresponders : List[Server], 
                     myproposal : Proposal, 
                     receivedproposal : Proposal) : Unit = {
    if (unresponders.isEmpty) {
      // we've heard back from majority!
      // propose a value, send accept requests to everyone

      if (receivedproposal.hasValue()) {
        myproposal.setValue(receivedproposal.getValue())
      }
      else {
        myproposal.setValue(this.name)
      }
      for (s <- quorum) {
        s ! ("accept request", myproposal)
      }
      proposerfinal(quorum, quorum, myproposal)
    }
    // still waiting...
    else {
      receiveWithin(timeout) {
        case ("promise", prop : Proposal) =>
          if (receivedproposal < prop) {
            proposerphase2(quorum, unresponders diff List(sender), myproposal, prop)
          }
          else {
            proposerphase2(quorum, unresponders diff List(sender), myproposal, receivedproposal)
          }
        case ("sorry", prop) =>
          if (myproposal == prop) {
            // this was my proposal, abort
            startround()
          }
          else {
            // perhaps message came from an old proposal?
            proposerphase2(quorum, unresponders, myproposal, receivedproposal)
          }
        case TIMEOUT => startround()
          // add case for other proposer's messages?
      }
    }
  }

  def proposerfinal(quorum : List[Server], unresponders : List[Server], proposal : Proposal) {
    if (unresponders.isEmpty) {
      // they all accepted! hurray! I'll also accept, and message everyone
      // TODO
      acceptedproposal = proposal

      Console.println("server " + name + ": We've chosen a leader! It's " + proposal.getValue)
//      for (s <- server) {
//        s ! ("chosen", proposal)
//      }
    }
    else {
      receiveWithin(timeout) {
        case ("accepted", prop) if prop == proposal => proposerfinal(quorum, unresponders diff List(sender), proposal)
        // received acknowledgement from past proposal?
        case ("accepted", prop) => proposerfinal(quorum, unresponders, proposal) 
        case ("sorry", _) => startround()
        case TIMEOUT => startround()
      }
    }
  }

  // I don't think we need to separate phases for acceptors
  def acceptor() : Unit = {
    receiveWithin(timeout) {
      case ("prepare request", prop: Proposal) => 
        if (biggestrecved <= prop) {
          reply(("promise", acceptedproposal))
          biggestrecved = prop
          acceptor()
        }
        else {
          reply(("sorry", prop))
          acceptor()
        }
      case ("accept request", prop: Proposal) =>
        if (biggestrecved <= prop) {
          acceptedproposal = prop
          biggestrecved = prop
          reply(("accepted", prop))
          acceptor()
        }
        else {
          reply(("sorry", prop))
          acceptor()
        }
      case TIMEOUT => startround()
    }
  }


  def startround() : Unit = {
    if (flipcoin()) {
      Console.println("server " + name + ": proposing")
      proposerphase1()
    }
    else {
      Console.println("server " + name + ": accepting")
      acceptor()
    }
  }


  def act(){
    startround()
  }
  def printleader(){
    Console.println("I'm Server " + name + " I accept " + acceptedproposal.getValue + " as my leader")
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

  Thread.sleep(5000)
  for (s <- servers) {
    s.printleader()
  }
}
