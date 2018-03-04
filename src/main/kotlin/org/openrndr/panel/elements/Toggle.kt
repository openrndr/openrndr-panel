package org.openrndr.panel.elements

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.FontImageMap
import org.openrndr.math.Vector2
import org.openrndr.panel.style.*
import org.openrndr.text.Cursor
import org.openrndr.text.Writer

import rx.subjects.PublishSubject


class Toggle : Element(ElementType("toggle")) {

    var label = ""
    var value = false
    var range = Range(0.0, 10.0)

    class ValueChangedEvent(val source:Toggle,
                            val oldValue:Boolean,
                            val newValue:Boolean)

    class Events {
        val valueChanged = PublishSubject.create<ValueChangedEvent>()
    }
    val events = Events()

    init {
        mouse.clicked.subscribe {
            value = !value
            draw.dirty = true
        }
        mouse.dragged.subscribe {
            value = !value
            draw.dirty = true
        }

    }
    override fun draw(drawer: Drawer) {
        drawer.stroke = null
        drawer.fill = ((computedStyle.color as? Color.RGBa)?.color)
        drawer.rectangle(layout.screenWidth-10.0, 2.0, 10.0, 4.0 )
        val x = layout.screenWidth - if(value) 5 else 10
        drawer.circle(Vector2(x, 3.0), 5.0)

        (root() as? Body)?.controlManager?.fontManager?.let {
            val font = it.font(computedStyle)

            val writer = Writer(drawer)
            drawer.fontMap = (font)
            drawer.fill = (ColorRGBa.BLACK)
            writer.cursor = Cursor(0.0, 0.0)
            writer.newLine()
            writer.text("$label")

        }

    }
}