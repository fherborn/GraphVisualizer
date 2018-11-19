import data.Node
import data.NodeType
import pathfinder.BasicPathFinder

class DijkstraAlgorithm : BasicPathFinder<Node>() {

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
                .forEach { edge ->
                    val node = edge.endNode
                    node.comingEdge = edge
                    node.g = current.g + edge.getLength()
                    node.f = node.g
                    node.parent = current

                    oList.find { it == node }?.let {existing ->
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

    private fun relax(existing: Node, node: Node, current: Node) {
        if (existing.g > node.g) {
            existing.g = node.g
            existing.parent = current
        }
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

}