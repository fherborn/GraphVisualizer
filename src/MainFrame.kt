import editor.EditorPanel
import javax.swing.JFrame

class MainFrame: JFrame("A* Algorithm") {

    private val mainPanel = EditorPanel()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        extendedState = MAXIMIZED_BOTH
        contentPane = mainPanel

    }

}