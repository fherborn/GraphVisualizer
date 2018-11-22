package paint

import editor.ToolBar
import data.*
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import pathfinder.PathFinder
import core.rotate
import core.toVector
import java.awt.*
import kotlin.math.roundToInt


class PaintManager {

    private class NodeBasicPaint(private val color: Color, private val textColor: Color, private val strokeWidth: Int, private val printTexts: Boolean): Paint<Node> {

        override fun paint(element: Node, graphics2D: Graphics2D) {
            graphics2D.color = color
            graphics2D.stroke = BasicStroke(strokeWidth.toFloat())
            graphics2D.drawOval(element.bounds.x, element.bounds.y, element.bounds.width, element.bounds.height)


            val string = element.name
            val stringHeight = graphics2D.fontMetrics.height
            val stringWidth = graphics2D.fontMetrics.stringWidth(string)

            graphics2D.color = textColor
            graphics2D.drawString(
                    string,
                    (element.bounds.x + element.bounds.width / 2) - stringWidth / 2 ,
                    element.bounds.y + (element.bounds.height - stringHeight)/2 + graphics2D.fontMetrics.ascent
            )
        }
        override fun paintInfo(element: Node, graphics2D: Graphics2D) {
            val infoDistance = 5.0

            if(printTexts) {
                val infoString = String.format("f:%.2f - g:%.2f - h:%.2f", element.h, element.g, element.h)
                val infoStringWidth = graphics2D.fontMetrics.stringWidth(infoString).toDouble()
                val textPosition = element.bounds.location.toVector().subtract(Vector2D((infoStringWidth/2) - element.bounds.width / 2, infoDistance))
                graphics2D.color = color
                graphics2D.drawString(infoString, textPosition.x.toInt(), textPosition.y.toInt())
            }
        }
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

    private class EdgeBasicPaint(private val color: Color, private val textColor: Color, private val strokeWidth: Int, private val printTexts: Boolean): Paint<Edge> {
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
        override fun paintInfo(element: Edge, graphics2D: Graphics2D) {

            if (printTexts) {
                val distance = element.getLength()

                val string = String.format("dis:%.2f", distance)

                val stringHeight = graphics2D.fontMetrics.height
                val stringWidth = graphics2D.fontMetrics.stringWidth(string)

                val infoOffset = 20.0
                val infoStart = element.bounds.location.toVector().subtract(Vector2D(stringWidth/2 + (stringHeight / 2.0), infoOffset))
                val infoBounds = Rectangle(infoStart.x.roundToInt(), infoStart.y.roundToInt(), stringWidth + stringHeight, stringHeight)

                graphics2D.color = color
                graphics2D.fillOval(infoBounds.x, infoBounds.y, infoBounds.height, infoBounds.height)
                graphics2D.fillOval(infoBounds.x + infoBounds.width, infoBounds.y, infoBounds.height, infoBounds.height)
                graphics2D.fillRect((infoBounds.x + infoBounds.height / 2.0).roundToInt(), infoBounds.y , infoBounds.width, infoBounds.height)


                graphics2D.color = textColor
                graphics2D.drawString(string, infoBounds.x + infoBounds.height, infoBounds.y+ graphics2D.fontMetrics.ascent)
            }
        }

    }

    private class EdgeHandlePaint(private val color: Color): Paint<Edge> {
        override fun paintInfo(element: Edge, graphics2D: Graphics2D) {}

        override fun paint(element: Edge, graphics2D: Graphics2D) {
            graphics2D.color = color
            graphics2D.fillOval(element.bounds.x, element.bounds.y, element.bounds.width, element.bounds.height)
        }
    }

    private class NodeHighlightBodyPaint(private val color: Color): Paint<Node> {
        override fun paint(element: Node, graphics2D: Graphics2D) {
            graphics2D.color = color
            graphics2D.fillOval(element.bounds.x, element.bounds.y, element.bounds.width, element.bounds.height)
        }
        override fun paintInfo(element: Node, graphics2D: Graphics2D) {}
    }

    fun updateColors(
            pathFinder: PathFinder<*>?,
            graph: Graph?,
            selectedNode: Node?,
            hoveredNode: Node?,
            selectedEdge: Edge?,
            hoveredEdge: Edge?,
            startNode: Node?,
            endNode: Node?,
            mode: ToolBar.Mode,
            texts: Boolean = false
    ) {

        graph?.nodes?.forEach {
            it.paint = when (it.nodeType) {
                NodeType.WALKABLE -> NodeBasicPaint(Color.DARK_GRAY, Color.DARK_GRAY, 2, texts)
                else -> PaintCompositum(NodeHighlightBodyPaint(Color.BLACK), NodeBasicPaint(Color.BLACK, Color.WHITE, 2, texts))
            }
        }
        graph?.edges?.forEach { it.paint = PaintCompositum(EdgeBasicPaint(Color.DARK_GRAY, Color.WHITE, 2, texts), EdgeHandlePaint(Color.DARK_GRAY)) }

        when (mode) {
            ToolBar.Mode.RUN -> {
                pathFinder?.getClosedList()?.forEach {
                    it.paint = PaintCompositum(NodeHighlightBodyPaint(Color.RED), NodeBasicPaint(Color.DARK_GRAY, Color.DARK_GRAY, 2, texts))
                    it.comingEdge?.paint = PaintCompositum(EdgeBasicPaint(Color.RED, Color.WHITE, 2, texts), EdgeHandlePaint(Color.RED))
                }

                pathFinder?.getOpenList()?.forEach {
                    it.paint = PaintCompositum(NodeHighlightBodyPaint(Color.YELLOW), NodeBasicPaint(Color.DARK_GRAY, Color.DARK_GRAY, 2, texts))
                    it.comingEdge?.paint = PaintCompositum(EdgeBasicPaint(Color.YELLOW, Color.BLACK, 2, texts), EdgeHandlePaint(Color.YELLOW))
                }

                pathFinder?.getPath()?.forEach {
                    it.paint = PaintCompositum(NodeHighlightBodyPaint(Color.GREEN), NodeBasicPaint(Color.DARK_GRAY, Color.DARK_GRAY, 2, texts))
                    it.comingEdge?.paint = PaintCompositum(EdgeBasicPaint(Color.GREEN, Color.BLACK, 2, texts), EdgeHandlePaint(Color.GREEN))
                }
                startNode?.paint = PaintCompositum(NodeHighlightBorderPaint(Color.BLUE, 4), NodeBasicPaint(Color.DARK_GRAY, Color.DARK_GRAY, 2, texts))
                endNode?.paint = PaintCompositum(NodeHighlightBorderPaint(Color.GREEN, 4), NodeBasicPaint(Color.DARK_GRAY, Color.DARK_GRAY, 2, texts))
            }
            ToolBar.Mode.NODE -> {
                hoveredNode?.paint = PaintCompositum(NodeHighlightBodyPaint(Color.LIGHT_GRAY), NodeBasicPaint(Color.DARK_GRAY, Color.DARK_GRAY, 2, texts))
            }
            ToolBar.Mode.EDGE -> {
                hoveredNode?.paint = PaintCompositum(NodeHighlightBorderPaint(Color.GREEN, 4), NodeBasicPaint(Color.DARK_GRAY, Color.DARK_GRAY, 2, texts))
                selectedNode?.paint = PaintCompositum(NodeHighlightBorderPaint(Color.GREEN, 4), NodeBasicPaint(Color.DARK_GRAY, Color.DARK_GRAY, 2, texts))
                selectedEdge?.paint = PaintCompositum(EdgeBasicPaint(Color.GREEN, Color.BLACK, 2, texts), EdgeHandlePaint(Color.GREEN))
                hoveredEdge?.paint = PaintCompositum(EdgeBasicPaint(Color.GREEN, Color.BLACK, 2, texts), EdgeHandlePaint(Color.GREEN))
            }
            ToolBar.Mode.MOVE -> {
                hoveredNode?.paint = PaintCompositum(NodeHighlightBodyPaint(Color.LIGHT_GRAY), NodeBasicPaint(Color.DARK_GRAY, Color.DARK_GRAY, 2, texts))
                selectedNode?.paint = PaintCompositum(NodeHighlightBodyPaint(Color.LIGHT_GRAY), NodeBasicPaint(Color.DARK_GRAY, Color.DARK_GRAY, 2, texts))
                selectedEdge?.paint = PaintCompositum(EdgeBasicPaint(Color.GREEN, Color.BLACK, 2, texts), EdgeHandlePaint(Color.GREEN))
                hoveredEdge?.paint = PaintCompositum(EdgeBasicPaint(Color.GREEN, Color.BLACK, 2, texts), EdgeHandlePaint(Color.GREEN))
            }
            else -> {
            }
        }


    }
}