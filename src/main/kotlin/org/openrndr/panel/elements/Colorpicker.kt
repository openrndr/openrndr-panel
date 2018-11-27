package org.openrndr.panel.elements

import io.reactivex.subjects.PublishSubject
import org.openrndr.MouseEvent
import org.openrndr.color.ColorHSVa
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.Drawer
import org.openrndr.draw.colorBuffer

class Colorpicker : Element {

    internal var colorMap: ColorBuffer? = null

    var label: String = "Color"

    var saturation = 0.5
    var color: ColorRGBa
        set(value) {
            realColor = value
            saturation = color.toHSVa().s
            generateColorMap()
            draw.dirty = true
        }
        get() {
            return realColor
        }

    private var realColor = ColorRGBa.WHITE
    private var focussed = false

    class ColorChangedEvent(val source: Colorpicker,
                            val oldColor: ColorRGBa,
                            val newColor: ColorRGBa)

    class Events {
        val colorChanged = PublishSubject.create<ColorChangedEvent>()
    }

    val events = Events()


    constructor() : super(ElementType("colorpicker")) {
        generateColorMap()

        mouse.exited.subscribe {
            focussed = false
        }

        mouse.scrolled.subscribe {
            if (colorMap != null) {
                //if (focussed) {
                saturation = (saturation - it.rotation.y * 0.01).coerceIn(0.0, 1.0)
                generateColorMap()
                colorMap?.shadow?.upload()
                it.cancelPropagation()
                draw.dirty = true
                //}
            }
        }

        fun pick(e: MouseEvent) {
            val dx = e.position.x - layout.screenX
            var dy = e.position.y - layout.screenY

            dy = 50.0 - dy
            val oldColor = color
            val hsv = ColorHSVa(360.0 / layout.screenWidth * dx, saturation, dy / 50.0)
            realColor = hsv.toRGBa()
            draw.dirty = true
            events.colorChanged.onNext(ColorChangedEvent(this, oldColor, realColor))
            e.cancelPropagation()
        }
        mouse.pressed.subscribe { it.cancelPropagation(); focussed = true }
        mouse.clicked.subscribe { it.cancelPropagation(); pick(it); focussed = true; }
        mouse.dragged.subscribe { it.cancelPropagation(); pick(it); focussed = true; }
    }

    private fun generateColorMap() {
        colorMap?.shadow?.let {
            for (y in 0..49) {
                for (x in 0 until it.colorBuffer.width) {
                    val hsv = ColorHSVa(360.0 / it.colorBuffer.width * x, saturation, (49 - y) / 49.0)
                    it.write(x, y, hsv.toRGBa())
                }
            }
            it.upload()
        }
    }

    override fun draw(drawer: Drawer) {
        if (colorMap == null) {
            colorMap = colorBuffer(layout.screenWidth.toInt(), 50, 1.0)
            generateColorMap()
        }

        drawer.image(colorMap!!, 0.0, 0.0)
        drawer.fill = (color)
        drawer.stroke = null
        drawer.shadeStyle = null
        drawer.rectangle(0.0, 50.0, layout.screenWidth, 20.0)

    }
}