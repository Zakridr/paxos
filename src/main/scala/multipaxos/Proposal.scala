package multipaxos


// Proposal is the tuple (slot_num, command)
class Proposal(slot:Int, c: Command){
    val s_num = slot
    val command = c

    def same_s(that:Proposal):Boolean = {return (this.s_num == that.s_num) }
    def same_cmd(that:Proposal):Boolean = {return (this.command.equal(that.command))}
    def equal(that: Proposal):Boolean = {return same_s(that) && same_cmd(that)}
    override def toString():String = { 
        val str = "(" + s_num + " " + command.toString + ")"
        return str
    }
    def print(){ 
        val str = this.toString
        println(str)
     }
    
}
