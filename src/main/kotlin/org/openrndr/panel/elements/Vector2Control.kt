package org.openrndr.panel.elements

import io.reactivex.subjects.PublishSubject
import org.openrndr.MouseEvent
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.math.clamp
import org.openrndr.math.map
import org.openrndr.panel.elements.Button
import org.openrndr.panel.elements.Element
import org.openrndr.panel.elements.ElementType
import org.openrndr.panel.elements.initElement
import java.util.*


class Vector2Control : Element(ElementType("vector2")) {
    class VectorControlEvent(val source: Vector2Control)


    var minX = -1.0
    var minY = -1.0
    var maxX = 1.0
    var maxY = 1.0

    var v = Vector2(-0.0, 0.0)

    val value: Vector2
        get() = Vector2(
                map(-1.0, 1.0, minX, maxX, v.x),
                map(-1.0, 1.0, minY, maxY, v.y)
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
    }

    class ValueChangedEvent(val source: Vector2Control,
                            val oldValue: Vector2,
                            val newValue: Vector2)


    val events = Events()

    class Events {
        val valueChanged = PublishSubject.create<ValueChangedEvent>()
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

        events.valueChanged.onNext(ValueChangedEvent(this, old, Vector2(
                map(-1.0, 1.0, minX, maxX, nx),
                map(-1.0, 1.0, minY, maxY, ny)
        )))

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

            for (y in 0 until 20) {
                drawer.lineSegment(
                        0.0,
                        layout.screenHeight / 20 * y,
                        layout.screenWidth - 1.0,
                        layout.screenHeight / 20 * y
                )
            }

            for (x in 0 until 20) {
                drawer.lineSegment(
                        layout.screenWidth / 20 * x,
                        0.0,
                        layout.screenWidth / 20 * x,
                        layout.screenHeight - 1.0
                )
            }

            // cross
            drawer.stroke = ColorRGBa.GRAY.shade(1.6)
            drawer.lineSegment(0.0, layout.screenHeight / 2.0, layout.screenWidth, layout.screenHeight / 2.0)
            drawer.lineSegment(layout.screenWidth / 2.0, 0.0, layout.screenWidth / 2.0, layout.screenHeight)

            // ball
            drawer.fill = ColorRGBa.PINK
            drawer.stroke = ColorRGBa.WHITE
            drawer.circle(ballPosition, 8.0)

            drawer.popStyle()
            drawer.popTransforms()
        }
    }
}
