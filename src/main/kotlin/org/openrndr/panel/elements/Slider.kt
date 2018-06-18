package org.openrndr.panel.elements

import org.openrndr.KEY_ARROW_LEFT
import org.openrndr.KEY_ARROW_RIGHT
import org.openrndr.KEY_ENTER
import org.openrndr.KEY_ESCAPE
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.LineCap
import org.openrndr.math.Vector2
import org.openrndr.panel.style.*
import org.openrndr.text.Cursor
import org.openrndr.text.Writer
import rx.subjects.PublishSubject

data class Range(val min: Double, val max: Double) {
    val span: Double get() = max - min
}

class Slider : Element(ElementType("slider")) {
    var label = ""
    var precision = 3
    var value = 0.0
        set(v) {
            val cleanV = v.coerceIn(range.min, range.max)
            val oldV = field
            val quantized = String.format("%.0${precision}f", cleanV).replace(",", ".").toDouble()
            field = quantized
            draw.dirty = true
            events.valueChanged.onNext(ValueChangedEvent(this, oldV, field))
        }
    var range = Range(0.0, 10.0)

    class ValueChangedEvent(val source: Slider,
                            val oldValue: Double,
                            val newValue: Double)

    class Events {
        val valueChanged: PublishSubject<ValueChangedEvent> = PublishSubject.create<ValueChangedEvent>()
    }

    val events = Events()

    private val margin = 7.0
    private var keyboardInput = ""

    init {
        mouse.pressed.subscribe {
            it.cancelPropagation()
        }
        mouse.clicked.subscribe {
            val t = (it.position.x - layout.screenX - margin) / (layout.screenWidth - 2.0 * margin)
            value = t * range.span + range.min
            it.cancelPropagation()
        }
        mouse.dragged.subscribe {
            val t = (it.position.x - layout.screenX - margin) / (layout.screenWidth - 2.0 * margin)
            value = t * range.span + range.min
            it.cancelPropagation()
        }

        mouse.scrolled.subscribe {
            if (Math.abs(it.rotation.y) < 0.001) {
                value += range.span * 0.001 * it.rotation.x
                it.cancelPropagation()
            }
        }
        keyboard.character.subscribe {
            keyboardInput += it.character
            draw.dirty = true
            it.cancelPropagation()
        }
        keyboard.pressed.subscribe {
            println(it.key)

            val delta = Math.pow(10.0, -(precision - 0.0))

            if (it.key == KEY_ARROW_RIGHT) {
                value += delta
            }
            if (it.key == KEY_ARROW_LEFT) {
                value -= delta
            }

            if (it.key == KEY_ESCAPE) {
                keyboardInput = ""
                draw.dirty = true
            }

            if (it.key == KEY_ENTER) {
                val number = keyboardInput.toDoubleOrNull()
                if (number != null) {
                    value = number.coerceIn(range.min, range.max)
                }
                keyboardInput = ""
                draw.dirty = true
            }

            if (it.key == 268) { // home
                value = range.min
                keyboardInput = ""
            }
            if (it.key == 269) { // end
                value = range.max
                keyboardInput = ""
            }
            it.cancelPropagation()
        }
    }

    override fun draw(drawer: Drawer) {
        val f = (root() as? Body)?.controlManager?.fontManager?.font(computedStyle)!!
        drawer.translate(0.0, (layout.screenHeight - (10.0 + f.height)) / 2)

        drawer.fill = ((computedStyle.color as Color.RGBa).color)
        drawer.stroke = ((computedStyle.color as Color.RGBa).color)
        drawer.strokeWeight = (8.0)
        drawer.lineCap = (LineCap.ROUND)
        val x = ((value - range.min) / range.span) * (layout.screenWidth - 2 * margin)

        drawer.stroke = ((computedStyle.color as Color.RGBa).color.opacify(0.25))
        drawer.lineSegment(margin + 0.0, 2.0, margin + layout.screenWidth - 2 * margin, 2.0)

        drawer.stroke = ((computedStyle.color as Color.RGBa).color.opacify(1.0))
        drawer.lineSegment(margin, 2.0, margin + x, 2.0)

        drawer.stroke = null
        drawer.circle(Vector2(margin + x, 2.0), 5.0)

        (root() as? Body)?.controlManager?.fontManager?.let {
            val font = it.font(computedStyle)
            val writer = Writer(drawer)
            drawer.fontMap = (font)
            drawer.fill = (ColorRGBa.BLACK)
            writer.cursor = Cursor(0.0, 10.0)
            writer.newLine()
            writer.text(label)

            if (keyboardInput.isEmpty()) {
                val valueFormatted = String.format("%.0${precision}f", value)
                val tw = writer.textWidth(valueFormatted)
                writer.cursor.x = (layout.screenWidth - tw)
                writer.text(valueFormatted)
            } else {
                val tw = writer.textWidth(keyboardInput)
                writer.cursor.x = (layout.screenWidth - tw)
                writer.text(keyboardInput)
            }
        }
    }
}