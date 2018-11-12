package paint

import editor.ToolBar
import data.*
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import pathfinder.PathFinder
import rotate
import toVector
import java.awt.*
import kotlin.math.roundToInt


class PaintManager {

    private class NodeBasicPaint(private val color: Color, private val testColor: Color, private val strokeWidth: Int): Paint<Node> {

        override fun paint(element: Node, graphics2D: Graphics2D) {
            graphics2D.color = color
            graphics2D.stroke = BasicStroke(strokeWidth.toFloat())
            graphics2D.drawOval(element.bounds.x, element.bounds.y, element.bounds.width, element.bounds.height)

            val string = element.name
            val stringHeight = graphics2D.fontMetrics.height
            val stringWidth = graphics2D.fontMetrics.stringWidth(string)

            graphics2D.color = testColor
            graphics2D.drawString(
                    string,
                    (element.bounds.x + element.bounds.width / 2) - stringWidth / 2 ,
                    element.bounds.y + (element.bounds.height - stringHeight)/2 + graphics2D.fontMetrics.ascent
            )
        }
        override fun paintInfo(element: Node, graphics2D: Graphics2D) {}
    }

    private class NodeHighlightBorderPaint(private val color: Color, private val strokeSize: Int): Paint<Node> {
        override fun paint(element: Node, graphics2D: Graphics2D) {

            graphics2D.stroke = BasicStroke(strokeSize.toFloat())

            graphics2D.color = color
            graphics2D.drawOval(
                    element.bounds.x - strokeSize, element.bounds.y - strokeSize,
                    element.bounds.width + strokeSize * 2, element.bounds.height + strokeSize * 2
            )
        }
        override fun paintInfo(element: Node, graphics2D: Graphics2D) {}
    }

    private class EdgeBasicPaint(private val color: Color, private val strokeWidth: Int): Paint<Edge> {
        override fun paint(element: Edge, graphics2D: Graphics2D) {


            val startNodeCenter = element.getStart()

            val endNodeCenter = element.getEnd()

            val normalizedDir = element.getDirection()


            val start = startNodeCenter.add(normalizedDir.scalarMultiply(element.startNode.bounds.width.toDouble() / 2))
            val end = endNodeCenter.subtract(normalizedDir.scalarMultiply(element.endNode.bounds.width.toDouble() / 2))

            graphics2D.color = color
            graphics2D.stroke = BasicStroke(strokeWidth.toFloat())
            graphics2D.drawLine(start.x.toInt(), start.y.toInt(), end.x.toInt(), end.y.toInt())

            val arrowSize = 4.0


            val arrow = listOf(
                    Vector2D(-arrowSize/2, 0.0),
                    Vector2D(arrowSize/2, 0.0),
                    Vector2D(0.0, arrowSize)
            )
                    .map { it.rotate(-Math.atan2(normalizedDir.x, normalizedDir.y)) }
                    .map { it.add(end.subtract(normalizedDir.scalarMultiply(arrowSize))) }


            graphics2D.drawPolygon(
                    arrow.map { it.x.toInt() }.toIntArray(),
                    arrow.map { it.y.toInt() }.toIntArray(),
                    arrow.size
            )
        }
        override fun paintInfo(element: Edge, graphics2D: Graphics2D) {}

    }

    private class EdgeHandlePaint(private val color: Color, private val textColor: Color, private val paintText: Boolean = false): Paint<Edge> {
        override fun paint(element: Edge, graphics2D: Graphics2D) {
            graphics2D.color = color
            graphics2D.fillOval(element.bounds.x, element.bounds.y, element.bounds.width, element.bounds.height)
        }
        override fun paintInfo(element: Edge, graphics2D: Graphics2D) {

            if (paintText) {
                val distance = element.getLength()

                val string = "${element.name}: $distance"

                val stringHeight = graphics2D.fontMetrics.height
                val stringWidth = graphics2D.fontMetrics.stringWidth(string)

                val infoOffset = 10.0
                val infoStart = element.bounds.location.toVector().add(Vector2D(0.0, 1.0).scalarMultiply(infoOffset))
                val infoBounds = Rectangle(infoStart.x.roundToInt(), infoStart.y.roundToInt(), stringWidth + stringHeight, stringHeight)

                graphics2D.fillOval(infoBounds.x, infoBounds.y, infoBounds.height, infoBounds.height)
                graphics2D.fillOval(infoBounds.x + infoBounds.width, infoBounds.y, infoBounds.height, infoBounds.height)
                graphics2D.fillRect(infoBounds.x + infoBounds.height / 2, infoBounds.y, infoBounds.width - infoBounds.height, infoBounds.height)


                graphics2D.color = textColor
                graphics2D.drawString(string, element.bounds.x + infoBounds.height / 2, element.bounds.y + (infoBounds.height - stringHeight)/2 + graphics2D.fontMetrics.ascent)
            }
        }
    }

    private class NodeHighlightBodyPaint(private val color: Color): Paint<Node> {
        override fun paint(element: Node, graphics2D: Graphics2D) {
            graphics2D.color = color
            graphics2D.fillOval(element.bounds.x, element.bounds.y, element.bounds.width, element.bounds.height)
        }
        override fun paintInfo(element: Node, graphics2D: Graphics2D) {}
    }

    private val nodeDefaultPaint = NodeBasicPaint(Color.DARK_GRAY, Color.DARK_GRAY, 2)
    private val nodeObstaclePaint = PaintCompositum(NodeHighlightBodyPaint(Color.BLACK), NodeBasicPaint(Color.BLACK, Color.WHITE, 2))
    private val nodeBodyHighlightPaint = PaintCompositum(NodeHighlightBodyPaint(Color.LIGHT_GRAY), nodeDefaultPaint)
    private val nodeBorderHighlightPaint = PaintCompositum(NodeHighlightBorderPaint(Color.GREEN, 4), nodeDefaultPaint)
    private val startNodePaint = PaintCompositum(NodeHighlightBorderPaint(Color.BLUE, 4), nodeDefaultPaint)
    private val endNodePaint = PaintCompositum(NodeHighlightBorderPaint(Color.GREEN, 4), nodeDefaultPaint)
    private val openListNodePaint = PaintCompositum(NodeHighlightBodyPaint(Color.YELLOW), nodeDefaultPaint)
    private val closedListNodePaint = PaintCompositum(NodeHighlightBodyPaint(Color.RED), nodeDefaultPaint)
    private val pathNodePaint = PaintCompositum(NodeHighlightBodyPaint(Color.GREEN), nodeDefaultPaint)

    private val edgeDefaultPaint = PaintCompositum(EdgeBasicPaint(Color.DARK_GRAY, 2), EdgeHandlePaint(Color.DARK_GRAY, Color.WHITE))
    private val edgeHighlightPaint = PaintCompositum(EdgeBasicPaint(Color.GREEN, 2), EdgeHandlePaint(Color.GREEN, Color.WHITE, true))
    private val openListEndgePaint = PaintCompositum(EdgeBasicPaint(Color.YELLOW, 2), EdgeHandlePaint(Color.YELLOW, Color.WHITE))
    private val closedListEdgePaint = PaintCompositum(EdgeBasicPaint(Color.RED, 2), EdgeHandlePaint(Color.RED, Color.WHITE))
    private val pathEdgePaint = PaintCompositum(EdgeBasicPaint(Color.GREEN, 2), EdgeHandlePaint(Color.GREEN, Color.WHITE))


    fun updateColors(
            pathFinder: PathFinder<*>?,
            graph: Graph?,
            selectedNode: Node?,
            hoveredNode: Node?,
            selectedEdge: Edge?,
            hoveredEdge: Edge?,
            startNode: Node?,
            endNode: Node?,
            mode: ToolBar.Mode
    ) {

        graph?.nodes?.forEach {
            it.paint = when (it.nodeType) {
                NodeType.WALKABLE -> nodeDefaultPaint
                else -> nodeObstaclePaint
            }
        }
        graph?.edges?.forEach { it.paint = edgeDefaultPaint }

        when (mode) {
            ToolBar.Mode.RUN -> {
                pathFinder?.getClosedList()?.forEach {
                    it.node.paint = closedListNodePaint
                    it.edge?.paint = closedListEdgePaint
                }

                pathFinder?.getOpenList()?.forEach {
                    it.node.paint = openListNodePaint
                    it.edge?.paint = openListEndgePaint
                }

                pathFinder?.getPath()?.forEach {
                    it.node.paint = pathNodePaint
                    it.edge?.paint = pathEdgePaint
                }
                startNode?.paint = startNodePaint
                endNode?.paint = endNodePaint
            }
            ToolBar.Mode.NODE -> {
                hoveredNode?.paint = nodeBodyHighlightPaint
            }
            ToolBar.Mode.EDGE -> {
                hoveredNode?.paint = nodeBorderHighlightPaint
                selectedNode?.paint = nodeBorderHighlightPaint
                selectedEdge?.paint = edgeHighlightPaint
                hoveredEdge?.paint = edgeHighlightPaint
            }
            ToolBar.Mode.MOVE -> {
                hoveredNode?.paint = nodeBodyHighlightPaint
                selectedNode?.paint = nodeBodyHighlightPaint
                selectedEdge?.paint = edgeHighlightPaint
                hoveredEdge?.paint = edgeHighlightPaint
            }
            else -> {
            }
        }


    }
}