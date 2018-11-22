package data

import paint.Drawable
import paint.Paint
import core.toVector
import java.awt.Graphics2D
import java.awt.Rectangle

data class Edge(val startNode: Node, val endNode: Node, val name: String): Drawable {


    var bounds = calcBounds()
    var paint: Paint<Edge>? = null

    override fun paint(graphics2D: Graphics2D) {

        bounds = calcBounds()

        paint?.paint(this, graphics2D)
    }

    override fun paintInfo(graphics2D: Graphics2D) {
        paint?.paintInfo(this, graphics2D)
    }


    fun getLength() = getStart().distance(getEnd())
    fun getDirection() = getEnd().subtract(getStart()).normalize()

    fun getStart() = startNode.bounds.location.toVector().add(startNode.bounds.size.toVector().scalarMultiply(0.5))
    fun getEnd() = endNode.bounds.location.toVector().add(endNode.bounds.size.toVector().scalarMultiply(0.5))
    fun handlePosition() = getStart().add(getDirection().scalarMultiply(getLength()/16.0*10.0))

    fun calcBounds(): Rectangle {

        val center = handlePosition()

        val rectSize = 10

        return Rectangle((center.x - rectSize / 2.0).toInt(), (center.y - rectSize / 2.0).toInt(), rectSize, rectSize)
    }

}