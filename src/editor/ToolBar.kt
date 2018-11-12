package editor

import java.awt.Color
import java.awt.FlowLayout
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.*


class ToolBar : JToolBar("Toolbar"), ItemListener {

    enum class Mode {
        UNKNOWN,
        NODE,
        EDGE,
        MOVE,
        OBSTACLE,
        RUN
    }

    enum class Algo {
        ASTAR,
        DIJKSTRA
    }

    var modeChangeListener: ((mode: Mode) -> Unit)? = null
    var generateGraphListener: (() -> Unit)? = null
    var resetGraphListener: (() -> Unit)? = null

    var startListener: ((algo: Algo, stepped: Boolean, stepTime: Int) -> Unit)? = null
    var stepListener: (() -> Unit)? = null
    var stopListener: (() -> Unit)? = null
    var clearListener: ((algo: Algo) -> Unit)? = null

    private val runUi = createRunUI()

    private var nodeModeButton = JRadioButton("Nodes")
    private var obstacleModeButton = JRadioButton("Obstacles")
    private var edgeModeButton = JRadioButton("Edges")
    private var moveModeButton = JRadioButton("Move")
    private var runModeButton = JRadioButton("Run")
    private var generateGraphButton = JButton("Generate Graph")
    private var resetGraphButton = JButton("Reset Graph")


    init {

        background = Color.lightGray
        layout = FlowLayout()

        val modeButtonGroup = ButtonGroup()
        modeButtonGroup.add(nodeModeButton)
        modeButtonGroup.add(obstacleModeButton)
        modeButtonGroup.add(edgeModeButton)
        modeButtonGroup.add(moveModeButton)
        modeButtonGroup.add(runModeButton)

        nodeModeButton.addItemListener(this)
        obstacleModeButton.addItemListener(this)
        edgeModeButton.addItemListener(this)
        moveModeButton.addItemListener(this)
        runModeButton.addItemListener(this)

        generateGraphButton.addActionListener { generateGraphListener?.invoke() }
        resetGraphButton.addActionListener { resetGraphListener?.invoke() }



        add(nodeModeButton)
        add(obstacleModeButton)
        add(edgeModeButton)
        add(moveModeButton)
        add(runModeButton)
        add(generateGraphButton)
        add(resetGraphButton)

        nodeModeButton.isSelected = true

    }

    fun getDrawMode() = when {
        nodeModeButton.isSelected -> Mode.NODE
        obstacleModeButton.isSelected -> Mode.OBSTACLE
        edgeModeButton.isSelected -> Mode.EDGE
        moveModeButton.isSelected -> Mode.MOVE
        runModeButton.isSelected -> Mode.RUN
        else -> Mode.UNKNOWN
    }

    override fun itemStateChanged(e: ItemEvent?) {

        val mode = getDrawMode()

        when(mode) {
            Mode.RUN -> add(runUi)
            else -> remove(runUi)
        }

        updateUI()
        modeChangeListener?.invoke(mode)
    }

    private fun createRunUI(): JPanel {

        val runUi = JPanel()
        runUi.layout = FlowLayout()

        val startButton = JButton("Start")
        val stopButton = JButton("Stop")
        val clearButton = JButton("Clear")
        val steppedButton = JCheckBox("Run Step by Step")
        val stepButton = JButton("Next")
        val aStarButton = JRadioButton("A*")
        val dijkstraButton = JRadioButton("Dijkstra")
        val timeInput = JTextField("100")

        val mode = when {
            dijkstraButton.isSelected -> Algo.DIJKSTRA
            else -> Algo.ASTAR
        }

        stepButton.addActionListener { stepListener?.invoke() }
        startButton.addActionListener { startListener?.invoke(mode, steppedButton.isSelected, timeInput.text.toInt()) }
        stopButton.addActionListener { stopListener?.invoke() }
        clearButton.addActionListener { clearListener?.invoke(mode) }


        runUi.add(startButton)
        runUi.add(stopButton)
        runUi.add(clearButton)
        runUi.add(steppedButton)
        runUi.add(JLabel("Step Time"))
        runUi.add(timeInput)

        steppedButton.addItemListener{
            if (steppedButton.isSelected) {
                runUi.add(stepButton)
            } else {
                runUi.remove(stepButton)
            }
            runUi.updateUI()
        }

        val algoGroup = ButtonGroup()
        algoGroup.add(aStarButton)
        algoGroup.add(dijkstraButton)

        aStarButton.isSelected = true

        runUi.add(aStarButton)
        runUi.add(dijkstraButton)
        return runUi
    }

}