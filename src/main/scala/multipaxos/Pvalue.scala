package multipaxos

// leader receive request from replicas and encapsulated it with Pvalue, send Pvalue in "accept request" to acceptors
class Pvalue(b: B_num, slot : Int, c: Command) {
    val b_num = b
    val s_num = slot // slot number
    val command = c

    def get_B_num(): B_num={
        return this.b_num
    }

    def <= (that: Pvalue) : Boolean = {
        return (this.b_num <= that.b_num)
    }

    def equal(that: Pvalue):Boolean = {
        return this.b_num.equal(that.b_num) && this.s_num==that.s_num && this.command.equal(that.command)
    }

    override def toString():String = { 
        val str = "(" + b_num.toString() + " " + s_num + " " + command.toString() + ")"
        return str
    }
    def print(){ 
        val str = this.toString
        println(str)
     }

}
