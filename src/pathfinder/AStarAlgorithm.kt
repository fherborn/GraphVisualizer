import data.Edge
import data.Node
import data.NodeType
import pathfinder.BasicPathFinder
import pathfinder.NodePack

class AStarAlgorithm : BasicPathFinder<AStarAlgorithm.NodeInfo>() {

    class NodeInfo(
            override val node: Node,
            override val edge: Edge?,
            var g: Double = 0.0,
            var h: Double = 0.0,
            var f: Double = 0.0,
            var parent: NodeInfo? = null
    ) : NodePack

    override fun onStart(startNode: Node, endNode: Node) {
        oList.add(NodeInfo(startNode, null))
        step()
    }

    override fun onReset() {
        oList = mutableListOf()
        cList = mutableListOf()
    }

    override fun onStep(startNode: Node, endNode: Node, stepped: Boolean, stepTime: Int) {
        if (oList.isEmpty()) return finished(null)
        val current = oList.minBy { it.f } ?: return finished(null)
        if (current.node == endNode) return finished(generatePath(current))

        oList.remove(current)
        cList.add(current)

        current.node.edges
                .filter { !cList.map { it.node }.contains(it.endNode) }
                .filter { it.endNode.nodeType == NodeType.WALKABLE }
                .forEach { edge ->
                    val node = NodeInfo(edge.endNode, edge)
                    node.g = current.g + edge.getLength()
                    node.h = h(node.node, endNode)
                    node.f = node.g + node.h
                    node.parent = current

                    oList.find { it.node == node.node }?.let {existing ->
                        relax(existing, node, current)
                    } ?: let {
                        oList.add(node)
                    }
                }

        if (!stepped) {
            Thread.sleep(stepTime.toLong())
            step()
        }
    }

    private fun relax(existing: NodeInfo, node: NodeInfo, current: NodeInfo) {
        if (existing.g > node.g) {
            existing.g = node.g
            existing.parent = current
        }
    }

    private fun generatePath(current: NodeInfo): List<NodeInfo> {
        val path = mutableListOf<NodeInfo>()
        var tmp: NodeInfo? = current
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