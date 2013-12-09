package paxutil

abstract class AppData {

    def applyOp(op : Symbol) : AppData
    
    def getString() : String
}
