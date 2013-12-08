package multipaxos


// a list of proposals and a set of s_num
class ProposalList(pplist : List[Proposal]) extends Serializable {
    var prlist = pplist
    var s_set = Set[Int]()//track the set of s_num

    def size():Int = {return prlist.length}
    def put(p: Proposal) = { 
        if(!contain(p)){
            prlist :+= p
            s_set += p.s_num
        }
    }
    // remove all proposals that equal p
    def remove(p: Proposal) = { 
        if(contain(p)){
            prlist = prlist.filterNot{e => e.equal(p)}
            s_set -= p.s_num 
        }
    }

    // a Xor b = {(s,p)| (s,p) belongs to b || (s,p) belongs to a && there is no such tuple (s,p') that belongs to b}
    def Xor (that: ProposalList):ProposalList = {
        var result = that
        prlist.foreach{e => { if(!result.exist_s_by_p(e)) 
                              result.put(e) }}
        return result
        
    }
    def contain(that: Proposal):Boolean = {
        prlist.foreach{e=>{if(e.equal(that)) return true}}
        return false
    }
    def exist_s_by_p(p:Proposal): Boolean={
        prlist.foreach{e => {if(e.same_s(p)) return true }}
        return false
    }
    def exist_s(slot:Int): Boolean={
        prlist.foreach{e => {if(e.s_num==slot) return true }}
        return false
    }
    def exist_cmd(c:Command):Boolean={
        prlist.foreach{e =>{if(e.command.equal(c)) return true}}
        return false
    }
    // get the proposal with s_num==slot
    def getBy_s(slot: Int): List[Proposal] = {
        for(temp <- prlist;if(temp.s_num == slot)) yield temp       
    }
     // get the proposal with command.equal(c)
    def getBy_cmd(c: Command): List[Proposal] = {
        for(temp <- prlist;if(temp.command.equal(c))) yield temp       
    }


    def print(){ prlist.foreach{e => e.print()} }
}
