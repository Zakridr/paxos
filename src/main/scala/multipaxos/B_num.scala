package multipaxos

// there are multiple clients, each of then can submit an operation with their client_id and command_id

// B_num is the tuple of (b_num, leader_id), each leader submit "prepare request" with b_num and b_num is increasing
// in the case that there are multiple leaders, to distinguish them we make b_num and leader_id a tuple
class B_num(b:Int, l:Int) extends Ordered[B_num]{
    val b_num = b
    val leader_id = l

    def compare(that: B_num):Int = {
        if (this.b_num != that.b_num) 
          this.b_num - that.b_num
        else
          this.leader_id - that.leader_id
    }

    def equal(that:B_num):Boolean={return this.b_num== that.b_num && this.leader_id ==that.leader_id}

    def getLeader() : Int = {
        return this.leader_id
    }

    override def toString():String = {return "("+b_num + " " + leader_id + ")"}

    def print(){ 
        val str = this.toString
        println(str) 
    }
}
