package paxutil

abstract class AppData[T] {

    def state : T
    def applyOp(op : Symbol) : AppData[T]
}
