package org.openrndr.panel.elements

import org.openrndr.Program
import org.openrndr.color.ColorHSVa
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.Drawer
import org.openrndr.draw.FontImageMap
import org.openrndr.panel.style.fontFamily
import org.openrndr.text.Writer
import rx.subjects.PublishSubject

class Colorpicker : Element {

    internal var colorBuffer: ColorBuffer? = null

    var label:String = "Color"

    var saturation = 0.5
    var color = ColorRGBa.WHITE
    var focussed = false

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

            if (colorBuffer != null) {
                if (focussed) {
                    saturation = (saturation - it.rotation.y * 0.01).coerceIn(0.0, 1.0)
                    generateColorMap()
                    colorBuffer?.shadow?.upload()
                    it.cancelPropagation()
                    draw.dirty = true
                }
            }

        }
        fun pick(e : Program.Mouse.MouseEvent) {
            val dx = e.position.x - layout.screenX
            var dy = e.position.y - layout.screenY

            dy = 50.0 - dy
            val oldColor = color
            val hsv = ColorHSVa(360.0 / layout.screenWidth * dx, saturation, dy / 50.0)
            color = hsv.toRGBa()
            draw.dirty = true
            events.colorChanged.onNext(ColorChangedEvent(this, oldColor, color))
            e.cancelPropagation()
        }
        mouse.clicked.subscribe { pick(it); focussed = true;  }
        mouse.dragged.subscribe { pick(it); focussed = true; }
    }

    fun generateColorMap() {



        colorBuffer?.shadow?.let {
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

        if (colorBuffer == null) {
            colorBuffer = ColorBuffer.create(layout.screenWidth.toInt(), 50, 1.0)
            generateColorMap()
        }

        drawer.image(colorBuffer!!, 0.0, 0.0)
        drawer.fill = (color)
        drawer.stroke = null
        drawer.shadeStyle = null
        drawer.rectangle(0.0,  50.0, layout.screenWidth, 20.0)

        val valueText = "-"


        val x = 0.0
        val y = 50+30.0
        val width = layout.screenWidth
        val font = (root() as Body).controlManager.fontManager.font(computedStyle)
        drawer.fill = (ColorRGBa.WHITE)

        drawer.fontMap = font
        val textWidth = Writer(drawer).textWidth(valueText)

        drawer.fontMap = font
        drawer.text(label, x, y)
        drawer.text(valueText, x + width - textWidth, y)


    }
}