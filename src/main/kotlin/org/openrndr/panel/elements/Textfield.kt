package org.openrndr.panel.elements

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.panel.style.Color
import org.openrndr.panel.style.background
import org.openrndr.panel.style.color
import org.openrndr.text.Writer

class Textfield : Element(ElementType("text-field")) {

    var value: String = ""
    var label: String = "label"

    class Events {

    }

    val events = Events()

    init {
        keyboard.pressed.subscribe {
            value += it.name
            it.cancelPropagation()
        }
    }

    override fun draw(drawer: Drawer) {
        drawer.fill = ((computedStyle.background as? Color.RGBa)?.color ?: ColorRGBa.PINK)
        (root() as? Body)?.controlManager?.fontManager?.let {
            val font = it.font(computedStyle)

            val writer = Writer(drawer)
            drawer.fontMap = (font)
            val textWidth = writer.textWidth(value)
            val textHeight = font.ascenderLength

            val offset = Math.round((layout.screenWidth - textWidth) / 2.0)
            val yOffset = Math.round((layout.screenHeight / 2) + textHeight / 2.0 - 2.0) * 1.0

            drawer.fill = ((computedStyle.color as? Color.RGBa)?.color ?: ColorRGBa.WHITE)
            drawer.text(label, 0.0 + offset, 0.0 + yOffset)
            drawer.stroke = ((computedStyle.color as? Color.RGBa)?.color ?: ColorRGBa.WHITE)
            drawer.strokeWeight = 1.0
        }
        drawer.rectangle(0.0, 0.0, layout.screenWidth, layout.screenHeight)
    }
}