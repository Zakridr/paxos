package paxos

class Proposal(p: Int, proposer_name : String) extends Ordered[Proposal] {
  val propnum = p
  val proposer = proposer_name

  var value = "blank"

  def compare(that : Proposal) = 
    if (this.propnum == that.propnum) this.proposer.compare(that.proposer)
    else this.propnum.compare(that.propnum)

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
