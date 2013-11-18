package paxos

class Proposal(p: Int, proposer_name : String) {
  val propnum = p
  val proposer = proposer_name

  var value = "blank"

  def <(that: Proposal): Boolean = {
    return (this.propnum < that.propnum || 
      (this.propnum == that.propnum && this.proposer < that.proposer))
  }

  def >(that: Proposal) : Boolean = {
    return that < this
  }

  def >=(that: Proposal) : Boolean = {
    return that <= this
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
