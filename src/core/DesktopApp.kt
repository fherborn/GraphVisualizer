package core

import editor.EditorPanel
import java.awt.Frame.MAXIMIZED_BOTH
import javax.swing.JFrame
import javax.swing.JFrame.EXIT_ON_CLOSE

object DesktopApp {
    @JvmStatic
    fun main(args: Array<String>) {
        displayGUI()
    }

    fun displayGUI(){
        val mainFrame = JFrame("A Stern Visualizer")
        val mainPanel = EditorPanel()
        mainFrame.defaultCloseOperation = EXIT_ON_CLOSE
        mainFrame.isVisible = true
        mainFrame.add(mainPanel)
        mainFrame.extendedState = MAXIMIZED_BOTH
    }
}