package paint

import java.awt.Graphics2D

interface Drawable {
    fun paint(graphics2D: Graphics2D)
    fun paintInfo(graphics2D: Graphics2D)
}