import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import java.awt.Dimension
import java.awt.Point


fun Point.toVector() = Vector2D(x.toDouble(), y.toDouble())
fun Dimension.toVector() = Vector2D(width.toDouble(), height.toDouble())
fun Vector2D.toDimension() = Dimension(x.toInt(), y.toInt())
fun Vector2D.toPoint() = Point(x.toInt(), y.toInt())
fun Vector2D.rotate(angle: Double) = Vector2D(
        Math.cos(angle) * x - Math.sin(angle) * y,
        Math.sin(angle) * x + Math.cos(angle) * y
)