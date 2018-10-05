package org.openrndr.panel.elements

import org.openrndr.KEY_BACKSPACE
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.LineCap
import org.openrndr.panel.style.*
import org.openrndr.text.Writer
import io.reactivex.subjects.PublishSubject

class Textfield : Element(ElementType("textfield")) {

    var value: String = ""
    var label: String = "label"

    class ValueChangedEvent(val source: Textfield, val oldValue: String, val newValue: String)
    class Events {
        val valueChanged: PublishSubject<ValueChangedEvent> = PublishSubject.create()
    }

    val events = Events()

    init {
        keyboard.repeated.subscribe {
            if (it.key == KEY_BACKSPACE) {
                if (!value.isEmpty())
                    value = value.substring(0, value.length - 1)
            }
            draw.dirty = true
            it.cancelPropagation()

        }
        keyboard.pressed.subscribe {
            println("${it.key} ${KEY_BACKSPACE}")
            if (it.key == KEY_BACKSPACE) {
                if (!value.isEmpty())
                    value = value.substring(0, value.length - 1)
            }
            draw.dirty = true
            it.cancelPropagation()
        }

        keyboard.character.subscribe {
            println("I got this event")
            val oldValue = value
            value += it.character
            events.valueChanged.onNext(ValueChangedEvent(this, oldValue, value))
            it.cancelPropagation()
        }

        mouse.pressed.subscribe {
            it.cancelPropagation()
        }
        mouse.clicked.subscribe {
            it.cancelPropagation()
        }
    }

    override fun draw(drawer: Drawer) {
        drawer.fill = computedStyle.effectiveBackground
        drawer.stroke = null
        drawer.rectangle(0.0, 0.0, layout.screenWidth, layout.screenHeight)

        (root() as? Body)?.controlManager?.fontManager?.let {
            val font = it.font(computedStyle)

            val writer = Writer(drawer)
            drawer.fontMap = (font)
            val textWidth = writer.textWidth(value)
            val textHeight = font.ascenderLength

            val offset = 5.0
            val yOffset = Math.round((layout.screenHeight / 2) + textHeight / 2.0 - 2.0) * 1.0

            drawer.fill = ((computedStyle.color as? Color.RGBa)?.color ?: ColorRGBa.WHITE)
            drawer.text("$label", 0.0 + offset, 0.0 + yOffset - textHeight * 1.5)


            drawer.fill = ((computedStyle.color as? Color.RGBa)?.color ?: ColorRGBa.WHITE)
            drawer.text("$value", 0.0 + offset, 0.0 + yOffset)
            drawer.stroke = ((computedStyle.color as? Color.RGBa)?.color ?: ColorRGBa.WHITE)
            drawer.strokeWeight = 1.0

            drawer.stroke = computedStyle.effectiveColor?.shade(0.25)
            drawer.lineCap = LineCap.ROUND
            drawer.lineSegment(0.0, yOffset + 4.0, layout.screenWidth, yOffset + 4.0)
        }
    }
}