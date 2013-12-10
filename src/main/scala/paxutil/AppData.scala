package paxutil

abstract class AppData extends Serializable {

    def applyOp(op : Symbol) : AppData
    
    def getString() : String
}
