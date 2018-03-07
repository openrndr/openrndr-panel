package org.openrndr.panel.elements

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.FontImageMap
import org.openrndr.draw.LineCap
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
    internal val margin = 7.0

    init {
        mouse.clicked.subscribe {
            value = !value
            draw.dirty = true
            events.valueChanged.onNext(ValueChangedEvent(this, !value, value))
        }
        mouse.dragged.subscribe {
            value = !value
            draw.dirty = true
            events.valueChanged.onNext(ValueChangedEvent(this, !value, value))
        }
    }

    override fun draw(drawer: Drawer) {
        drawer.stroke = null
        drawer.fill = ((computedStyle.color as? Color.RGBa)?.color)
        drawer.strokeWeight = 8.0

        drawer.lineCap = LineCap.ROUND
        drawer.stroke = ((computedStyle.color as? Color.RGBa)?.color)?.opacify(0.25)
        drawer.lineSegment(Vector2(layout.screenWidth-margin-16,10.0),Vector2(layout.screenWidth-margin,10.0))
        drawer.stroke = ((computedStyle.color as? Color.RGBa)?.color)

        if (value) {
            val x = 12.0
            drawer.lineSegment(Vector2(layout.screenWidth - margin - 16 + x, 10.0), Vector2(layout.screenWidth - margin + x - 12, 10.0))
        }
        else {
            drawer.circle(Vector2(layout.screenWidth-margin-16.0,10.0),5.0)
        }

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