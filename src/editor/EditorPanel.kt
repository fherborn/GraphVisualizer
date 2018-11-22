package editor
import core.MainFrame
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JPanel
import javax.swing.SwingUtilities



class  EditorPanel(val mainFrame: MainFrame) : JPanel() {

    private val toolbar = ToolBar()

    private var graphPanel = GraphPanel()

    init {
        //isOpaque = false
        layout = BorderLayout()

        toolbar.modeChangeListener = { graphPanel.mode = it }
        toolbar.resetGraphListener = { graphPanel.resetGraph() }
        toolbar.generateGraphListener = { graphPanel.generateGraph() }
        toolbar.startListener = { algo, stepped, stepTime -> graphPanel.startAlgo(algo, stepped, stepTime) }
        toolbar.stepListener = { graphPanel.pathFinder?.step() }
        toolbar.stopListener = { graphPanel.pathFinder?.stop() }
        toolbar.clearListener = { graphPanel.resetAlgo(it) }
        toolbar.paintTextsListener = {
            graphPanel.paintTexts = it
            graphPanel.update()
        }

        toolbar.saveAsImageListener = { saveImage() }

        graphPanel.mode = toolbar.getDrawMode()

        add(toolbar, BorderLayout.NORTH)
        add(graphPanel, BorderLayout.CENTER)
    }



    fun getScreenShot(rect: Rectangle) = Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice).createScreenCapture(rect)


    fun saveImage() {
        try {

            val framesizeV = 30
            val framesizeH = 60

            val rect = graphPanel.graph.graphRect()
            val position = rect.location
            SwingUtilities.convertPointToScreen(position, graphPanel)

            rect.y =  position.y - framesizeV
            rect.x = position.x - framesizeH
            rect.height = rect.height + 2 * framesizeV
            rect.width = rect.width + 2  * framesizeH

            val imagebuf: BufferedImage = getScreenShot(rect)
            ImageIO.write(imagebuf, "PNG", File("${UUID.randomUUID()}.png"))

        } catch (e1: AWTException) {
            e1.printStackTrace()
        } catch (e: Exception) {
            println("error")
        }

    }


}