package org.openrndr.panel.elements

import io.reactivex.subjects.PublishSubject
import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.math.clamp
import org.openrndr.math.map
import org.openrndr.panel.style.Color
import org.openrndr.panel.style.color
import org.openrndr.text.Writer
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt


class PlanarPad : Element(ElementType("planar-pad")) {
    var minX = -1.0
    var minY = -1.0
    var maxX = 1.0
    var maxY = 1.0

    // A smaller number so it doesn't clutter the UI by default
    var precision = 1


    // What to call this? The normalized value?
    var v = Vector2(-0.0, 0.0)

    val value: Vector2
        get() = Vector2(
                map(-1.0, 1.0, minX, maxX, v.x).round(precision),
                map(-1.0, 1.0, minY, maxY, v.y).round(precision)
        )

    init {
        mouse.clicked.subscribe {
            it.cancelPropagation()
            pick(it)
        }

        mouse.dragged.subscribe {
            it.cancelPropagation()
            pick(it)
        }

        mouse.pressed.subscribe {
            it.cancelPropagation()
        }

        keyboard.pressed.subscribe { handleKeyEvent(it) }
        keyboard.repeated.subscribe { handleKeyEvent(it) }
    }

    class ValueChangedEvent(val source: PlanarPad,
                            val oldValue: Vector2,
                            val newValue: Vector2)


    val events = Events()

    class Events {
        val valueChanged = PublishSubject.create<ValueChangedEvent>()
    }


    private fun handleKeyEvent(keyEvent: KeyEvent) {
        val delta = 10.0.pow(-precision)
        if (keyEvent.key == KEY_ARROW_RIGHT) {
            v = Vector2(v.x + delta, v.y)
        }

        if (keyEvent.key == KEY_ARROW_LEFT) {
            v = Vector2(v.x - delta, v.y)
        }

        if (keyEvent.key == KEY_ARROW_UP) {
            v = Vector2(v.x, v.y - delta)
        }

        if (keyEvent.key == KEY_ARROW_DOWN) {
            v = Vector2(v.x, v.y + delta)
        }

        draw.dirty = true
        keyEvent.cancelPropagation()
    }

    private fun pick(e: MouseEvent) {
        val old = value

        // Difference
        val dx = e.position.x - layout.screenX
        var dy = e.position.y - layout.screenY

        // Normalize to -1 - 1
        val nx = clamp(dx / layout.screenWidth * 2.0 - 1.0, -1.0, 1.0)
        val ny = clamp(dy / layout.screenHeight * 2.0 - 1.0, -1.0, 1.0)

        v = Vector2(nx, ny)

        events.valueChanged.onNext(ValueChangedEvent(this, old, value))
        draw.dirty = true
    }

    override val widthHint: Double?
        get() = 200.0


    private val ballPosition: Vector2
        get() = Vector2(
                map(-1.0, 1.0, 0.0, layout.screenWidth, v.x),
                map(-1.0, 1.0, 0.0, layout.screenHeight, v.y)
        )

    override fun draw(drawer: Drawer) {
        computedStyle.let {
            drawer.pushTransforms()
            drawer.pushStyle()
            drawer.fill = ColorRGBa.GRAY
            drawer.stroke = null
            drawer.strokeWeight = 0.0

            drawer.rectangle(0.0, 0.0, layout.screenWidth, layout.screenHeight)


            // lines grid
            drawer.stroke = ColorRGBa.GRAY.shade(1.2)
            drawer.strokeWeight = 1.0

            for (y in 0 until 21) {
                drawer.lineSegment(
                        0.0,
                        layout.screenHeight / 20 * y,
                        layout.screenWidth - 1.0,
                        layout.screenHeight / 20 * y
                )
            }

            for (x in 0 until 21) {
                drawer.lineSegment(
                        layout.screenWidth / 20 * x,
                        0.0,
                        layout.screenWidth / 20 * x,
                        layout.screenHeight - 1.0
                )
            }

            // cross
            drawer.stroke = ColorRGBa.GRAY.shade(1.6)
//            drawer.lineSegment(0.0, layout.screenHeight / 2.0, layout.screenWidth, layout.screenHeight / 2.0)
//            drawer.lineSegment(layout.screenWidth / 2.0, 0.0, layout.screenWidth / 2.0, layout.screenHeight)

            // angle line from center
            drawer.lineSegment(Vector2(layout.screenHeight / 2.0, layout.screenWidth / 2.0), ballPosition)

            // ball
            drawer.fill = ColorRGBa.PINK
            drawer.stroke = ColorRGBa.WHITE
            drawer.circle(ballPosition, 8.0)

            val label = "${value.x.round(precision)}, ${value.y.round(precision)}"
            (root() as? Body)?.controlManager?.fontManager?.let {
                val font = it.font(computedStyle)
                val writer = Writer(drawer)
                drawer.fontMap = (font)
                val textWidth = writer.textWidth(label)
                val textHeight = font.ascenderLength

                drawer.fill = ((computedStyle.color as? Color.RGBa)?.color ?: ColorRGBa.WHITE).opacify(
                        if (disabled in pseudoClasses) 0.25 else 1.0
                )


                drawer.text(label, Vector2(layout.screenWidth - textWidth - 4.0, layout.screenHeight - textHeight + 6.0))
            }

            drawer.popStyle()
            drawer.popTransforms()
        }
    }
}


fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}
