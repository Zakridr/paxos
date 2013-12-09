package multipaxos

import paxutil.ActorData

class Command(client_id: ActorData, command_id : Int, operation: Symbol) extends Serializable {
    // cid and cmid together uniquely decide the command
    val cid = client_id
    val cmid = command_id
    val op = operation   
    // decide whether the two commands are the same
    def equal(that: Command) : Boolean ={
        return ( (this.cid equals that.cid) && this.cmid == that.cmid)

    }
    def getOp() = this.op
    override def toString():String = {return "(" + cid + " " + cmid + " " + op + ")"}
    def print(){ 
        val str = this.toString()
        println(str)
    }
}
