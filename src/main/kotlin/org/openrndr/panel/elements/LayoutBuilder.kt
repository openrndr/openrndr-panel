package org.openrndr.panel.elements

import org.openrndr.draw.Drawer
import org.openrndr.panel.ControlManager
import java.awt.TextField

fun Element.layout(init: Element.() -> Unit) {
    init()
}

fun layout(controlManager: ControlManager, init: Body.() -> Unit) : Body {
    val body = Body(controlManager)
    body.init()
    return body
}

fun <T : Element> Element.initElement(element: T, init: T.() -> Unit) : Element {
    element.init()
    append(element)
    return element
}

fun Element.button(id:String?=null, label:String="button", init: Button.() -> Unit):Button {
    val button = Button().apply {
        this.id = id
        this.label = label
    }
    initElement(button, init)
    return button

}
fun Element.slider(init: Slider.() -> Unit) = initElement(Slider(), init) as Slider
fun Element.toggle(init: Toggle.() -> Unit) = initElement(Toggle(), init) as Toggle

fun Element.colorpicker(init: Colorpicker.() -> Unit) = initElement(Colorpicker(), init)
fun Element.colorpickerButton(init: ColorpickerButton.() -> Unit) = initElement(ColorpickerButton(), init)

fun Canvas.draw(f:(Drawer)->Unit) {
    this.userDraw = f
}
fun Element.canvas(vararg classes:String, init: Canvas.() ->Unit) {
    val canvas = Canvas()
    classes.forEach { canvas.classes.add(ElementClass(it)) }
    canvas.init()
    append(canvas)
}

fun Element.dropdownButton(id:String?=null, label:String="button", init: DropdownButton.() -> Unit) = initElement(DropdownButton().apply {
    this.id = id
    this.label = label
}, init)

fun Element.envelopeButton(init: EnvelopeButton.()->Unit) = initElement(EnvelopeButton().apply {}, init)
fun Element.envelopeEditor(init: EnvelopeEditor.()->Unit) = initElement(EnvelopeEditor().apply {}, init)

fun Element.textfield(init: Textfield.()->Unit) = initElement(Textfield(), init)

fun DropdownButton.item(init:Item.() -> Unit) : Item {
    val item = Item().apply(init)


    append(item)
    return item
}


fun Element.div(vararg classes:String, init: Div.() -> Unit):Div {
    val div = Div()
    classes.forEach { div.classes.add(ElementClass(it)) }
    initElement(div, init)
    return div
}


inline fun <reified T:TextElement> Element.textElement(id:String?=null, init:T.()->String):T {
    val te = T::class.java.newInstance()
    te.id = id
    te.text(te.init())
    append(te)
    return te
}


fun Element.p(id:String?=null, init:P.()->String):P = textElement(id, init)
fun Element.h1(id:String?=null, init:H1.()->String):H1 = textElement(id, init)
fun Element.h2(id:String?=null, init:H2.()->String):H2 = textElement(id, init)
fun Element.h3(id:String?=null, init:H3.()->String):H3 = textElement(id, init)
