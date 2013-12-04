package multipaxos

class Command(client_id: Int, command_id : Int, operation: String) {
    // cid and cmid together uniquely decide the command
    val cid = client_id
    val cmid = command_id
    val op = operation   
    // decide whether the two commands are the same
    def equal(that: Command) : Boolean ={
        return (this.cid == that.cid && this.cmid == that.cmid)

    }
    def getOp():String={ return this.op}
    override def toString():String = {return "(" + cid + " " + cmid + " " + op + ")"}
    def print(){ 
        val str = this.toString()
        println(str)
    }
}
