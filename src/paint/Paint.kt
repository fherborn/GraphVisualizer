package paint

import java.awt.*


interface Paint<T> {
    fun paint(element: T, graphics2D: Graphics2D)
    fun paintInfo(element: T, graphics2D: Graphics2D)
}


