package org.openrndr.panel.elements

import io.lacuna.artifex.Vec
import org.openrndr.draw.Drawer
import org.openrndr.panel.ControlManager

fun Element.layout(init: Element.() -> Unit) {
    init()
}

fun layout(controlManager: ControlManager, init: Body.() -> Unit): Body {
    val body = Body(controlManager)
    body.init()
    return body
}

fun <T : Element> Element.initElement(classes: Array<out String>, element: T, init: T.() -> Unit): Element {
    append(element)
    element.classes.addAll(classes.map { ElementClass(it) })
    element.init()
    return element
}

fun Element.button(vararg classes: String, label: String = "button", init: Button.() -> Unit): Button {
    val button = Button().apply {
        this.classes.addAll(classes.map { ElementClass(it) })
        this.id = id
        this.label = label
    }
    initElement(classes, button, init)
    return button
}

fun Button.clicked(listener: (Button.ButtonEvent) -> Unit) {
    events.clicked.subscribe(listener)
}

fun Element.slider(vararg classes: String, init: Slider.() -> Unit) = initElement(classes, Slider(), init) as Slider
fun Element.toggle(vararg classes: String, init: Toggle.() -> Unit) = initElement(classes, Toggle(), init) as Toggle

fun Element.colorpicker(vararg classes: String, init: Colorpicker.() -> Unit) = initElement(classes, Colorpicker(), init)
fun Element.colorpickerButton(vararg classes: String, init: ColorpickerButton.() -> Unit) = initElement(classes, ColorpickerButton(), init)

fun Element.vector2(vararg classes: String, init: Vector2Control.() -> Unit) =
        initElement(classes, Vector2Control(), init) as Vector2Control

fun Canvas.draw(f: (Drawer) -> Unit) {
    this.userDraw = f
}

fun Element.canvas(vararg classes: String, init: Canvas.() -> Unit) {
    val canvas = Canvas()
    classes.forEach { canvas.classes.add(ElementClass(it)) }
    canvas.init()
    append(canvas)
}

fun Element.dropdownButton(vararg classes: String, id: String? = null, label: String = "button", init: DropdownButton.() -> Unit) = initElement(classes, DropdownButton().apply {
    this.id = id
    this.label = label
}, init)

fun Element.envelopeButton(vararg classes: String, init: EnvelopeButton.() -> Unit) = initElement(classes, EnvelopeButton().apply {}, init)
fun Element.envelopeEditor(vararg classes: String, init: EnvelopeEditor.() -> Unit) = initElement(classes, EnvelopeEditor().apply {}, init)

fun Element.textfield(vararg classes: String, init: Textfield.() -> Unit) = initElement(classes, Textfield(), init)

fun DropdownButton.item(init: Item.() -> Unit): Item {
    val item = Item().apply(init)


    append(item)
    return item
}

fun Element.div(vararg classes: String, init: Div.() -> Unit): Div {
    val div = Div()
    initElement(classes, div, init)
    return div
}

inline fun <reified T : TextElement> Element.textElement(classes: Array<out String>, init: T.() -> String): T {
    val te = T::class.java.newInstance()
    te.classes.addAll(classes.map { ElementClass(it) })
    te.text(te.init())
    append(te)
    return te
}

fun Element.p(vararg classes: String, init: P.() -> String): P = textElement(classes, init)
fun Element.h1(vararg classes: String, init: H1.() -> String): H1 = textElement(classes, init)
fun Element.h2(vararg classes: String, init: H2.() -> String): H2 = textElement(classes, init)
fun Element.h3(vararg classes: String, init: H3.() -> String): H3 = textElement(classes, init)

fun Element.vector2DPad(vararg classes: String, init: Vector2DPad.() -> Unit) = initElement(classes, Vector2DPad(), init) as Vector2DPad
