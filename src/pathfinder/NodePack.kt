package pathfinder

import data.Edge
import data.Node

interface NodePack {
    val node: Node
    val edge: Edge?
}