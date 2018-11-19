package pathfinder

import data.Graph
import data.Node

abstract class BasicPathFinder<T>: PathFinder<T> where T: Node {


    override var stepListener: (() -> Unit)? = null
    private var stepped = false
    private var startNode: Node? = null
    private var endNode: Node? = null
    private var started = false
    private var stepTime = 0

    private var path = listOf<T>()

    protected var oList = mutableListOf<T>()
    protected var cList = mutableListOf<T>()

    protected abstract fun onStart(startNode: Node, endNode: Node)
    protected abstract fun onStep(startNode: Node, endNode: Node, stepped: Boolean, stepTime: Int)
    protected abstract fun onReset()

    override fun getPath(): List<T> = path
    override fun getOpenList(): List<T> = oList
    override fun getClosedList(): List<T> = cList

    override fun start(graph: Graph, startNode: Node, endNode: Node, stepped: Boolean, stepTime: Int) {
        if(started) {
            stop()
            onReset()
        }

        this.stepTime = stepTime
        this.stepped = stepped
        this.startNode = startNode
        this.endNode = endNode
        this.started = true

        onStart(startNode, endNode)
    }

    @Synchronized
    override fun step() {
        if(!started) return
        startNode?.let { sn ->
            endNode?.let { en ->
                stepListener?.invoke()
                Thread {
                    onStep(sn, en, stepped, stepTime)
                }.start()
            }
        }
    }

    protected fun finished(path: List<T>?) {
        stop()
        path?.let { this.path = it }
    }

    override fun stop() {
        started = false
    }

}

