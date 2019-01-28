import core.toVector
import data.Edge
import data.Node
import data.NodeType
import pathfinder.BasicPathFinder

class AStarAlgorithm : BasicPathFinder<Node>() {

    override fun onStart(startNode: Node, endNode: Node) {
        oList.add(startNode)
        step()
    }

    override fun onReset() {
        oList = mutableListOf()
        cList = mutableListOf()
    }

    override fun onStep(startNode: Node, endNode: Node, stepped: Boolean, stepTime: Int) {
        if (oList.isEmpty()) return finished(null)
        val current = oList.minBy { it.f } ?: return finished(null)
        if (current == endNode) return finished(generatePath(current))

        oList.remove(current)
        cList.add(current)

        current.edges
                .filter { !cList.contains(it.endNode) }
                .filter { it.endNode.nodeType == NodeType.WALKABLE }
                .forEach { findNext(current, endNode, it) }

        if (!stepped) {
            Thread.sleep(stepTime.toLong())
            step()
        }
    }

    private fun findNext(current: Node, endNode: Node, edge: Edge) {

        val neighbour = edge.endNode
        val g = current.g + edge.getLength()

        if (oList.contains(neighbour)) {
            if (g < neighbour.g) {
                updateNode(neighbour, g, edge, current, endNode)
            }
        } else {
            updateNode(neighbour, g, edge, current, endNode)
            oList.add(neighbour)
        }
    }

    private fun updateNode(neighbour: Node, g: Double, comingEdge: Edge, current: Node, endNode: Node) {
        relax(neighbour, g, endNode)
        neighbour.comingEdge = comingEdge
        neighbour.parent = current
    }

    private fun relax(neighbour: Node, g: Double, endNode: Node) {
        neighbour.g = g
        neighbour.h = h(neighbour, endNode)
        neighbour.f = neighbour.g + neighbour.h
    }

    private fun generatePath(current: Node): List<Node> {
        val path = mutableListOf<Node>()
        var tmp: Node? = current
        while (tmp != null) {
            path.add(tmp)
            tmp = tmp.parent
        }
        return path.reversed()
    }

    //H is the heuristic — estimated distance from the current node to the end node.
    private fun h(node: Node, endNode: Node) =
            node.bounds.location.toVector().distance(endNode.bounds.location.toVector())

}