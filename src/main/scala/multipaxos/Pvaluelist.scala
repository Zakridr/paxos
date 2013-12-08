package multipaxos

// a list of Pvalue, acceptors will have a copy of it
class PvalueList() extends Serializable {
    var pvlist = List[Pvalue]()

    def size():Int = {return pvlist.length}
    def majority_size():Int = {return (pvlist.length/2) }
    def put(p:Pvalue) = { 
        if(!contain(p))
            pvlist :+= p 
    }
    //remove all pvalue that equal p
    def remove(p: Pvalue) = { 
        if(contain(p))
            pvlist = pvlist.filterNot{e=>e.equal(p)} 
    }
    def contain(p: Pvalue):Boolean = {
        pvlist.foreach{e => {if(e.equal(p)) return true}}
        return false
    }
    def putList(l:PvalueList)={
        l.pvlist.foreach{e => put(e)}
    }

    def filter_By_s_num(ss_num:Int):PvalueList={
        var result = new PvalueList();
        pvlist.foreach{e=> {if(e.s_num>=ss_num) result.put(e)}}
        return result

    }


    // Pmax(pvlist) = {(s,p)| exist b: (b,s,p) belongs to pvlist && for any b', p': (b',s,p') belongs to pvlist=> b'<=b}
    def Pmax() : ProposalList = {
        var templist = List[Pvalue]()
        var result = new ProposalList(List[Proposal]())
        val temp = pvlist.groupBy(x=>x.s_num)//temp is a map collection, mapping from s->pvalue
        temp.foreach{case (k,v) => templist :+= v.maxBy(_.b_num) }//get the max b_num for each slot
        templist.foreach {e => result.put(new Proposal(e.s_num, e.command))}
        return result

    }

    def print(){
        pvlist.foreach{e => e.print()}
    }
}
