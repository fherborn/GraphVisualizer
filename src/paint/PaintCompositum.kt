package paint

import java.awt.Graphics2D

class PaintCompositum<T>(private vararg val paints: Paint<T>): Paint<T> {
    override fun paintInfo(element: T, graphics2D: Graphics2D) { paints.forEach { it.paintInfo(element, graphics2D) } }
    override fun paint(element: T, graphics2D: Graphics2D) { paints.forEach { it.paint(element, graphics2D) } }
}
