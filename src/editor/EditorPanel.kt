package editor
import AStarAlgorithm
import data.*
import editor.ToolBar.Mode.*
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import paint.PaintManager
import pathfinder.PathFinder
import toPoint
import toVector
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.util.*
import javax.swing.JPanel
import javax.swing.SwingUtilities


class EditorPanel : JPanel(), MouseListener, MouseMotionListener {

    private val toolbar = ToolBar()


    private var nodeCounter = 0
    private var edgeCounter = 0

    private var graph = Graph()

    private var currentMousePosition: Point = Point()

    private var selectedNode: Node? = null
    private var selectedEdge: Edge? = null
    private var hoveredNode: Node? = null
    private var hoveredEdge: Edge? = null

    private var startNode: Node? = null
    private var endNode: Node? = null

    private var mode: ToolBar.Mode = toolbar.getDrawMode()

    private var pathFinder: PathFinder<*>? = null

    private var pathFinderColorManager = PaintManager()


    init {
        layout = BorderLayout()

        toolbar.modeChangeListener = { mode = it }
        toolbar.resetGraphListener = { resetGraph() }
        toolbar.generateGraphListener = { generateGraph() }
        toolbar.startListener = { algo, stepped, stepTime -> startAlgo(algo, stepped, stepTime) }
        toolbar.stepListener = { pathFinder?.step() }
        toolbar.stopListener = { pathFinder?.stop() }
        toolbar.clearListener = { resetAlgo(it) }

        add(toolbar, BorderLayout.NORTH)

        addMouseListener(this)
        addMouseMotionListener(this)
    }

    private fun startAlgo(algo: ToolBar.Algo, stepped: Boolean, stepTime: Int) {
        val start = startNode
        val end = endNode
        if (start != null && end != null) {
            resetAlgo(algo)
            pathFinder?.start(graph, start, end, stepped, stepTime)
            pathFinder?.stepListener = { update() }
        }
        update()
    }

    private fun resetAlgo(algo: ToolBar.Algo) {
        pathFinder?.stop()
        pathFinder = when (algo) {
            ToolBar.Algo.DIJKSTRA -> null
            ToolBar.Algo.ASTAR -> AStarAlgorithm()
        }
        update()
    }

    private fun generateGraph() {
        resetGraph()
        val nodeSize = Node.defaultSize
        val offset = 50
        val topSpace = 30
        val random = Random()

        val nodeCountX = bounds.width / (nodeSize.x + offset).toInt()
        val nodeCountY = bounds.height / (nodeSize.y + offset).toInt()

        val nodes = (0 until nodeCountX).map { x ->
            (0 until nodeCountY).map { y ->
                val position = Vector2D(x * nodeSize.x + (x + 1) * offset, y * nodeSize.y + (y + 1) * offset + topSpace)
                val node = Node(position, getNodeName())
                node.nodeType = when(random.nextInt(10)) {
                    in 0..7 -> NodeType.WALKABLE
                    else -> NodeType.OBSTACLE
                }
                graph.addNode(node)
                node

            }
        }

        fun connectNode(x: Int, y: Int, node: Node) {
            (x - 1..x + 1).forEach { newX ->
                (y - 1..y + 1).forEach { newY ->
                    if (newX >= 0 && newX < nodes.size && newY >= 0 && newY < nodes[newX].size && nodes[newX][newY] != node) {
                        graph.addEdge(node, nodes[newX][newY], getEdgeName())
                    }
                }
            }
        }

        (0 until nodes.size).forEach { x ->
            (0 until nodes[x].size).forEach { y ->
                connectNode(x, y, nodes[x][y])
            }
        }

        update()

    }

    private fun resetGraph() {
        graph = Graph()
        startNode = null
        endNode = null
        nodeCounter = 0
        edgeCounter = 0
        resetSelections()
    }

    private fun resetSelections() {
        selectedNode = null
        selectedEdge = null
        update()
    }

    private fun update() {
        invalidate()
        repaint()
    }

    private fun moveEdge(e: MouseEvent, edge: Edge) {
        val startNodeCenter = edge.startNode.bounds.location.toVector().add(edge.startNode.bounds.size.toVector().scalarMultiply(0.5))
        val endNodeCenter = edge.endNode.bounds.location.toVector().add(edge.startNode.bounds.size.toVector().scalarMultiply(0.5))
        val edgeHalfLength = startNodeCenter.distance(endNodeCenter) / 2
        val direction = startNodeCenter.subtract(endNodeCenter).normalize()

        edge.startNode.bounds.location = e.point.toVector().add(direction.scalarMultiply(edgeHalfLength)).subtract(edge.startNode.bounds.size.toVector().scalarMultiply(0.5)).toPoint()
        edge.endNode.bounds.location = e.point.toVector().subtract(direction.scalarMultiply(edgeHalfLength)).subtract(edge.endNode.bounds.size.toVector().scalarMultiply(0.5)).toPoint()

    }

    private fun mousePositionChanged(point: Point) {
        currentMousePosition = point
        updateCollision(point)
    }

    private fun updateCollision(point: Point) {
        val mouseRect = Rectangle(point, Dimension(2, 2))
        hoveredEdge = graph.edges.find { it.bounds.intersects(mouseRect) }
        hoveredNode = graph.nodes.find { it.bounds.intersects(mouseRect) }
    }

    private fun moveNode(e: MouseEvent, node: Node) {
        node.bounds.location = e.point.toVector().subtract(node.bounds.size.toVector().scalarMultiply(0.5)).toPoint()
    }

    private fun getEdgeName() = "${('a'..'z').toList()[edgeCounter / 26 % 26]}${('a'..'z').toList()[edgeCounter++ % 26]}"

    private fun getNodeName() = "${('A'..'Z').toList()[nodeCounter / 26 % 26]}${('A'..'Z').toList()[nodeCounter++ % 26]}"

    private fun calculateNodePosition(e: MouseEvent) = e.point.toVector().subtract(Node.defaultSize.scalarMultiply(0.5))

    override fun paint(g: Graphics) {
        super.paint(g)

        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)


        when (mode) {
            EDGE -> {
                selectedNode?.let {
                    g2.color = Color.GREEN
                    g2.drawLine(it.bounds.x + it.bounds.width / 2, it.bounds.y + it.bounds.height / 2, currentMousePosition.x, currentMousePosition.y)
                }
            }
            else -> {

            }
        }

        pathFinderColorManager.updateColors(
                pathFinder,
                graph,
                selectedNode,
                hoveredNode,
                selectedEdge,
                hoveredEdge,
                startNode,
                endNode,
                mode
        )
        graph.paint(g2)
        graph.paintInfo(g2)
    }

    override fun mouseMoved(e: MouseEvent) {
        mousePositionChanged(e.point)
        update()
    }

    override fun mouseDragged(e: MouseEvent) {
        mousePositionChanged(e.point)

        when {
            mode == MOVE -> {
                selectedEdge?.let { moveEdge(e, it) } ?: let { selectedNode?.let { moveNode(e, it) } }
            }
            mode == OBSTACLE && SwingUtilities.isLeftMouseButton(e) -> {
                hoveredNode?.let { it.nodeType = NodeType.OBSTACLE }
            }
            mode == OBSTACLE && SwingUtilities.isRightMouseButton(e) -> {
                hoveredNode?.let { it.nodeType = NodeType.WALKABLE }
            }
        }
        update()
    }

    override fun mouseEntered(e: MouseEvent?) {
    }

    override fun mouseClicked(e: MouseEvent) {
        when {
            mode == NODE && SwingUtilities.isLeftMouseButton(e) -> {
                hoveredNode?: let { graph.addNode(Node(calculateNodePosition(e), getNodeName())) }
            }
            mode == NODE && SwingUtilities.isRightMouseButton(e) -> {
                hoveredNode?.let { graph.removeNode(it) }
            }
            mode == OBSTACLE && SwingUtilities.isLeftMouseButton(e) -> {
                hoveredNode?.let { it.nodeType = NodeType.OBSTACLE  }
            }
            mode == OBSTACLE && SwingUtilities.isRightMouseButton(e) -> {
                hoveredNode?.let { it.nodeType = NodeType.WALKABLE }
            }
            mode == EDGE && SwingUtilities.isRightMouseButton(e) -> {
                hoveredEdge?.let { graph.removeEdge(it) }
            }
            mode == RUN && SwingUtilities.isLeftMouseButton(e) -> {
                when {
                    hoveredNode?.nodeType == NodeType.OBSTACLE -> {
                    }
                    startNode == null -> addStartNode(hoveredNode)
                    startNode != null && endNode == null -> addEndNode(hoveredNode)
                    startNode != null && endNode != null -> {
                        addStartNode(hoveredNode)
                        addEndNode(null)
                    }
                }
            }
            else -> {
            }
        }
        update()
    }

    private fun addStartNode(node: Node?) {
        startNode = node

    }

    private fun addEndNode(node: Node?) {
        endNode = node
    }


    override fun mouseExited(e: MouseEvent?) {
    }


    override fun mousePressed(e: MouseEvent) {
        when (mode) {
            EDGE -> selectedNode = selectedNode ?: hoveredNode
            MOVE -> {
                selectedNode = selectedNode ?: hoveredNode
                selectedEdge = selectedEdge ?: hoveredEdge
            }
            else -> {
            }
        }
    }

    override fun mouseReleased(e: MouseEvent) {
        when (mode) {
            EDGE -> {
                hoveredNode?.let { endNote ->
                    selectedNode?.let { startNode ->
                        graph.addEdge(startNode, endNote, getEdgeName())
                    }
                }
            }
            else -> {
            }
        }
        resetSelections()
    }


}