package pathfinder

import data.Edge
import data.Graph
import data.Node



interface PathFinder<T> where T: NodePack {
    var stepListener: (() -> Unit)?
    fun start(graph: Graph, startNode: Node, endNode: Node, stepped: Boolean, stepTime: Int)
    fun step()
    fun stop()
    fun getOpenList(): List<T>
    fun getClosedList(): List<T>
    fun getPath(): List<T>
}