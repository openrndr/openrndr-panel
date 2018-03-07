package org.openrndr.panel.elements

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.panel.style.*
import org.openrndr.text.Writer
import rx.subjects.PublishSubject

class Item:Element(ElementType("item")) {
    var label:String = ""
    var data:Any? = null

    class PickedEvent(val source:Item)


    class Events {
        val picked = PublishSubject.create<Item.PickedEvent>()
    }

    val events = Events()


    fun picked() {
        events.picked.onNext(PickedEvent(this))
    }
}

class DropdownButton: Element(ElementType("dropdown-button")) {

    var label: String = "OK"
    var value: Item? = null

    class ValueChangedEvent(val source:DropdownButton, val value:Item)

    class Events {
        val valueChanged = PublishSubject.create<ValueChangedEvent>()
    }

    val events = Events()
    init {
        mouse.clicked.subscribe {
            if (children.none { it is SlideOut })
            append(SlideOut(0.0,screenArea.height, screenArea.width, 200.0, this))
            else {
                (children.first { it is SlideOut } as SlideOut?)?.dispose()
            }
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
        drawer.stroke = null
        drawer.rectangle(0.0, 0.0, screenArea.width, screenArea.height)

        (root() as? Body)?.controlManager?.fontManager?.let {
            val font = it.font(computedStyle)

            val writer = Writer(drawer)
            drawer.fontMap = (font)

            val text = "$label: ${(value?.label)?:"<choose>"}"

            val textWidth = writer.textWidth(text)
            val textHeight = font.ascenderLength

            val offset = Math.round((layout.screenWidth-textWidth) / 2.0)
            val yOffset = Math.round((layout.screenHeight/2) + textHeight/2.0) - 2.0

            drawer.fill = ((computedStyle.color as? Color.RGBa)?.color ?: ColorRGBa.WHITE)

            drawer.text(text, 0.0 + offset   , 0.0 + yOffset)
        }

    }
    class SlideOut(val x:Double, val y:Double, val width:Double, val height:Double, parent:Element):Element(ElementType("slide-out")) {

        init {
            mouse.scrolled.subscribe {
                        scrollTop -= it.rotation.y
                        scrollTop = Math.max(0.0, scrollTop)
                draw.dirty = true
                it.cancelPropagation()

            }

            style = StyleSheet().apply {
                position = Position.ABSOLUTE
                left = LinearDimension.PX(x)
                top = LinearDimension.PX(y)
                width = LinearDimension.PX(this@SlideOut.width)
                height = LinearDimension.Auto//LinearDimension.PX(this@SlideOut.height)
                overflow = Overflow.Scroll
                zIndex = ZIndex.Value(1)
                background = Color.Inherit
            }

            (parent as DropdownButton).items().forEach {
                append(Button().apply {
                    data = it
                    label = it.label
                    events.clicked.subscribe {
                        parent.value = it.source.data as Item
                        parent.events.valueChanged.onNext(ValueChangedEvent(parent, it.source.data as Item))
                        (data as Item).picked()
                        dispose()
                    }
                })
            }
        }

        override fun draw(drawer:Drawer) {
            drawer.fill = ((computedStyle.background as? Color.RGBa)?.color ?: ColorRGBa.PINK)
            drawer.stroke = null
            drawer.strokeWeight = 0.0
            drawer.rectangle(0.0, 0.0, screenArea.width, screenArea.height)
            drawer.strokeWeight = 1.0
        }

        fun dispose() {
            parent?.remove(this)
        }
    }


}