package org.openrndr.panel.elements

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.FontImageMap
import org.openrndr.panel.style.*
import org.openrndr.shape.Rectangle
import org.openrndr.text.Writer
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.yield
import org.openrndr.launch
import kotlin.reflect.KMutableProperty0

class Item : Element(ElementType("item")) {
    var label: String = ""
    var data: Any? = null

    class PickedEvent(val source: Item)

    class Events {
        val picked = PublishSubject.create<Item.PickedEvent>()
    }

    val events = Events()

    fun picked() {
        events.picked.onNext(PickedEvent(this))
    }
}

class DropdownButton : Element(ElementType("dropdown-button")) {

    var label: String = "OK"
    var value: Item? = null

    class ValueChangedEvent(val source: DropdownButton, val value: Item)

    class Events {
        val valueChanged = PublishSubject.create<ValueChangedEvent>()
    }

    val events = Events()

    init {
        mouse.pressed.subscribe {
            it.cancelPropagation()
        }

        mouse.clicked.subscribe {
            val itemCount = items().size



            if (children.none { it is SlideOut }) {
                val height = items().size * 20.0 + (items().size - 1) * 10
                if (screenPosition.y < root().layout.screenHeight - height) {
                    append(SlideOut(0.0, screenArea.height, screenArea.width, height, this))
                } else {

                    append(SlideOut(0.0, screenArea.height - height, screenArea.width, height, this))
                }
            }
            else {
                (children.first { it is SlideOut } as SlideOut?)?.dispose()
            }
        }
    }

    override val widthHint:Double?
        get()  {
            computedStyle.let { style ->
                val fontUrl = (root() as? Body)?.controlManager?.fontManager?.resolve(style.fontFamily)?:"broken"
                val fontSize = (style.fontSize as? LinearDimension.PX)?.value?: 16.0
                val fontMap = FontImageMap.fromUrl(fontUrl, fontSize)
                val writer = Writer(null)

                writer.box = Rectangle(0.0,
                        0.0,
                        Double.POSITIVE_INFINITY,
                        Double.POSITIVE_INFINITY)

                val text = "$label  ${(value?.label) ?: "<choose>"}"
                writer.drawStyle.fontMap = fontMap
                writer.newLine()
                writer.text(text, visible = false)

                return writer.cursor.x + 10.0
            }
        }


    override fun append(element: Element) {
        when (element) {
            is Item, is SlideOut -> super.append(element)
            else -> throw RuntimeException("only item and slideout")
        }
        super.append(element)
    }

    fun items(): List<Item> = children.filter { it is Item }.map { it as Item }

    override fun draw(drawer: Drawer) {

        drawer.fill = ((computedStyle.background as? Color.RGBa)?.color ?: ColorRGBa.PINK)
        drawer.stroke = null
        drawer.rectangle(0.0, 0.0, screenArea.width, screenArea.height)

        (root() as? Body)?.controlManager?.fontManager?.let {
            val font = it.font(computedStyle)

            val writer = Writer(drawer)
            drawer.fontMap = (font)

            val text = "${(value?.label) ?: "<choose>"}"

            val textWidth = writer.textWidth(text)
            val textHeight = font.ascenderLength

            val offset = Math.round((layout.screenWidth - textWidth))
            val yOffset = Math.round((layout.screenHeight / 2) + textHeight / 2.0) - 2.0

            drawer.fill = ((computedStyle.color as? Color.RGBa)?.color ?: ColorRGBa.WHITE)

            drawer.text("$label", 5.0, 0.0 + yOffset)
            drawer.text(text, -5.0 + offset, 0.0 + yOffset)
        }
    }

    class SlideOut(val x: Double, val y: Double, val width: Double, val height: Double, parent: Element) : Element(ElementType("slide-out")) {
        init {
            mouse.scrolled.subscribe {
                scrollTop -= it.rotation.y
                scrollTop = Math.max(0.0, scrollTop)
                draw.dirty = true
                it.cancelPropagation()
            }

            mouse.exited.subscribe {
                it.cancelPropagation()
                dispose()
            }

            style = StyleSheet(CompoundSelector.DUMMY).apply {
                position = Position.ABSOLUTE
                left = LinearDimension.PX(x)
                top = LinearDimension.PX(y)
                width = LinearDimension.PX(this@SlideOut.width)
                height = LinearDimension.PX(this@SlideOut.height)
                overflow = Overflow.Scroll
                zIndex = ZIndex.Value(1000)
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

        override fun draw(drawer: Drawer) {
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

fun <E:Enum<E>> DropdownButton.bind(property: KMutableProperty0<E>, map:Map<E, String>) {
    val options = mutableMapOf<E, Item>()
    map.forEach { k,v ->
        options[k] = item {
            label = v
            events.picked.subscribe {
                property.set(k)
            }
        }
    }
    var currentValue = property.get()
    value = options[currentValue]
    draw.dirty = true

    (root() as? Body)?.controlManager?.program?.launch {
        while(true) {
            val cval = property.get()
            if (cval != currentValue) {
                currentValue = cval
                value = options[cval]
                draw.dirty = true
            }
            yield()
        }
    }
}
