package data

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import paint.Drawable
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle

class Graph(val nodes: MutableList<Node> = mutableListOf()) : Drawable {


    val edges: MutableList<Edge> = mutableListOf()

    fun addNode(node: Node) = nodes.add(node)
    fun removeNode(node: Node) {
        nodes.remove(node)
        removeEdges(node.edges)
        removeEdges(node.incomingEdges)
    }

    fun addEdge(startNode: Node, endNode: Node, name: String) = Edge(startNode, endNode, name).also {
        edges.add(it)
        startNode.edges.add(it)
        endNode.incomingEdges.add(it)
    }

    fun removeEdges(edges: List<Edge>) {
        this.edges.removeAll(edges)
        edges.map { it.startNode }.forEach { it.edges.removeAll(edges) }
        edges.map { it.endNode }.forEach { it.incomingEdges.removeAll(edges) }
    }

    fun removeEdge(edge: Edge) {
        edges.remove(edge)
        edge.startNode.edges.remove(edge)
        edge.endNode.incomingEdges.remove(edge)
    }

    override fun paint(graphics2D: Graphics2D) {
        nodes.forEach { it.paint(graphics2D) }
        edges.forEach { it.paint(graphics2D) }
    }

    override fun paintInfo(graphics2D: Graphics2D) {
        nodes.forEach { it.paintInfo(graphics2D) }
        edges.forEach { it.paintInfo(graphics2D) }
    }

    fun topLeft() = Point(
            nodes.map { it.bounds.x }.min() ?: 0,
            nodes.map { it.bounds.y }.min() ?: 0
    )

    fun bottomRight() = Point(
            nodes.map { it.bounds.x + it.bounds.width }.max() ?: 0,
            nodes.map { it.bounds.y + it.bounds.height }.max() ?: 0
    )

    fun graphRect(): Rectangle {
        val topLeft = topLeft()
        val bottomRight = bottomRight()

        return Rectangle(
                topLeft.x,
                topLeft.y,
                bottomRight.x - topLeft.x,
                bottomRight.y - topLeft.y
        )
    }

}