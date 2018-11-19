package data

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import paint.Drawable
import paint.Paint
import java.awt.*



open class Node(position: Vector2D, val name: String, val edges: MutableList<Edge> = mutableListOf(), val incomingEdges: MutableList<Edge> = mutableListOf()): Drawable {

    var bounds = Rectangle(position.x.toInt(), position.y.toInt(), defaultSize.x.toInt(), defaultSize.y.toInt())
    var nodeType = NodeType.WALKABLE
    var paint: Paint<Node>? = null
    var f: Double = 0.0
    var g: Double = 0.0
    var h: Double = 0.0
    var parent: Node? = null
    var comingEdge: Edge? = null

    override fun paint(graphics2D: Graphics2D) {
        paint?.paint(this, graphics2D)
    }

    override fun paintInfo(graphics2D: Graphics2D) {
        paint?.paintInfo(this, graphics2D)
    }

    companion object {
        val defaultSize: Vector2D = Vector2D(48.0, 48.0)
    }

}