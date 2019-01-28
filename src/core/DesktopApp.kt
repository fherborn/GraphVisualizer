package core

import editor.EditorPanel
import java.awt.Frame.MAXIMIZED_BOTH
import javax.swing.JFrame
import javax.swing.JFrame.EXIT_ON_CLOSE
import javax.swing.SwingUtilities

object DesktopApp {
    @JvmStatic
    fun main(args: Array<String>) {
        displayGUI()
    }

    fun displayGUI(){
        println("Hier2")
        val mainFrame = JFrame("A Stern Visualizer")
        val mainPanel = EditorPanel()
        mainFrame.defaultCloseOperation = EXIT_ON_CLOSE
        println("Hier3")
        mainFrame.isVisible = true
        mainFrame.add(mainPanel)
        mainFrame.extendedState = MAXIMIZED_BOTH
        println("Hier4")
    }
}