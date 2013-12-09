package paxutil

class DemoData(init : Colour) extends AppData[Colour] {

    val state = init
    val cdelta = 20

    def applyOp(op : Symbol) = 
        op match {
            case 'incR => new DemoData(state.changeRed(cdelta))
            case 'incG => new DemoData(state.changeGreen(cdelta))
            case 'incB => new DemoData(state.changeBlue(cdelta))
            case 'decR => new DemoData(state.changeRed(-cdelta))
            case 'decG => new DemoData(state.changeGreen(-cdelta))
            case 'decB => new DemoData(state.changeBlue(-cdelta))
            case _     => throw new RuntimeException("oops... no op")
        }
}
