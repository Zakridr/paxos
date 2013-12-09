package paxutil
import java.awt.Color

class Colour(red : Int, green : Int, blue : Int) extends Color(red : Int, green : Int, blue : Int) {
    def bound(colval : Int) = {
        if (colval > 255) 255 else if (colval < 0) 0 else colval
    }

    def changeRed(red : Int) = {
        new Colour (bound(this.getRed + red), this.getGreen, this.getBlue)
    }

    def changeGreen(green : Int) = {
        new Colour (this.getRed, bound(this.getGreen + green), this.getBlue)
    }

    def changeBlue(blue : Int) = {
        new Colour (getRed, getGreen, bound(getBlue + blue))
    }
}
