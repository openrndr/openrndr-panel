package org.openrndr.panel.elements

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.LineCap
import org.openrndr.panel.style.*
import org.openrndr.text.Writer
import rx.subjects.PublishSubject

class ColorpickerButton: Element(ElementType("colorpicker-button")) {

    var label: String = "OK"
    var color: ColorRGBa = ColorRGBa(0.5, 0.5, 0.5)

    class ColorChangedEvent(val source:ColorpickerButton, val color:ColorRGBa)

    class Events {
        val valueChanged = PublishSubject.create<ColorChangedEvent>()
    }

    val events = Events()
    init {
        mouse.clicked.subscribe {
            append(SlideOut(0.0,screenArea.height, screenArea.width, 200.0, this))
        }
    }
    override fun append(element:Element) {
        when(element) {
            is Item, is SlideOut -> super.append(element)
            else -> throw RuntimeException("only item and slideout")
        }
        super.append(element)
    }

    fun items(): List<Item> = children.filter { it is Item}.map { it as Item}

    override fun draw(drawer: Drawer) {

        drawer.fill = ((computedStyle.background as? Color.RGBa)?.color ?: ColorRGBa.PINK)
        drawer.rectangle(0.0, 0.0, screenArea.width, screenArea.height)

        (root() as? Body)?.controlManager?.fontManager?.let {
            val font = it.font(computedStyle)

            val writer = Writer(drawer)
            drawer.fontMap = (font)

            val text = "$label"

            val textWidth = writer.textWidth(text)
            val textHeight = font.ascenderLength

            val offset = Math.round((layout.screenWidth-textWidth) / 2.0)
            val yOffset = Math.round((layout.screenHeight/2) + textHeight/2.0) - 2.0

            drawer.fill = ((computedStyle.color as? Color.RGBa)?.color ?: ColorRGBa.WHITE)
            drawer.fontMap = font
            drawer.text(text, 0.0 + offset   , 0.0 + yOffset)
            drawer.stroke = (color)
            drawer.pushStyle()
            drawer.strokeWeight = (4.0)
            drawer.lineCap = (LineCap.ROUND)
            drawer.lineSegment(2.0, layout.screenHeight-2.0, layout.screenWidth - 2.0, layout.screenHeight-2.0)
            drawer.popStyle()
        }

    }
    class SlideOut(val x:Double, val y:Double, val width:Double, val height:Double, parent:Element):Element(ElementType("slide-out")) {

        init {
            style = StyleSheet().apply {
                position = Position.ABSOLUTE
                left = LinearDimension.PX(x)
                top = LinearDimension.PX(y)
                width = LinearDimension.PX(this@SlideOut.width)
                height = LinearDimension.Auto//LinearDimension.PX(this@SlideOut.height)
                overflow = Overflow.Scroll
                zIndex = ZIndex.Value(1)
                background = Color.RGBa(ColorRGBa(0.3,0.3,0.3))
            }

                append(Colorpicker().apply {
                    label =(parent as ColorpickerButton).label
                    events.colorChanged.subscribe {
                        parent.color = color
                        parent.events.valueChanged.onNext(ColorChangedEvent(parent, parent.color))
                    }

                })

                append(Button().apply {
                    label = "done"
                    events.clicked.subscribe {
                        //parent.value = it.source.data as Item
                        //parent.events.valueChanged.onNext(ValueChangedEvent(parent, it.source.data as Item))
                        dispose()
                    }
                })
        }

        override fun draw(drawer:Drawer) {
            drawer.fill = ((computedStyle.background as? Color.RGBa)?.color ?: ColorRGBa.PINK)
            drawer.rectangle(0.0, 0.0, screenArea.width, screenArea.height)
        }

        fun dispose() {
            parent?.remove(this)
        }
    }


}