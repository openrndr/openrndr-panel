package org.openrndr.panel

import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.math.Vector2
import org.openrndr.panel.elements.Body
import org.openrndr.panel.elements.Element
import org.openrndr.panel.elements.ElementPseudoClass
import org.openrndr.panel.elements.visit
import org.openrndr.panel.layout.Layouter
import org.openrndr.panel.style.*
import org.openrndr.shape.Rectangle
import javax.swing.text.Style

class ControlManager : Extension {
    var body: Element? = null
    val layouter = Layouter()
    val fontManager = FontManager()
    private val renderTargetCache = HashMap<Element, RenderTarget>()

    var contentScale = 1.0
    lateinit var renderTarget: RenderTarget

    inner class MouseInput {

        var dragTarget:Element? = null

        fun scroll(event: Program.Mouse.MouseEvent) {
            fun traverse(element: Element) {

                element.children.forEach(::traverse)
                if (!event.propagationCancelled) {
                    if (event.position in element.screenArea && element.computedStyle.display != Display.NONE) {
                        element.mouse.scrolled.onNext(event)
                    }
                }
            }
            body?.let(::traverse)
        }

        fun click(event: Program.Mouse.MouseEvent) {
            dragTarget = null
            fun traverse(element: Element) {
                //  if (element.children.isEmpty()) {
                //} else {
                if (element.computedStyle.display != Display.NONE) {
                    element.children.forEach(::traverse)
                }
                if (!event.propagationCancelled && event.position in element.screenArea && element.computedStyle.display != Display.NONE) {
                    element.mouse.clicked.onNext(event)
                }

                //}
            }
            body?.let(::traverse)
        }

        fun press(event: Program.Mouse.MouseEvent) {
            fun traverse(element: Element) {
                if (element.computedStyle.display != Display.NONE) {
                    element.children.forEach(::traverse)
                }
                if (!event.propagationCancelled && event.position in element.screenArea && element.computedStyle.display != Display.NONE) {
                    println("setting drag target to: $element")
                    dragTarget = element
                    element.mouse.pressed.onNext(event)
                }
            }
            body?.let(::traverse)
        }

        fun release(event:Program.Mouse.MouseEvent) {

        }

        fun drag(event: Program.Mouse.MouseEvent) {
            dragTarget?.mouse?.dragged?.onNext(event)
//            fun traverse(element: Element) {
//                if (element.computedStyle.display != Display.NONE) {
//
//                    if (element.children.isEmpty()) {
//                        if (!event.propagationCancelled && event.position in element.screenArea && element.computedStyle.display != Display.NONE) {
//                            element.mouse.dragged.onNext(event)
//                        }
//                    } else {
//                        element.children.forEach(::traverse)
//                    }
//                }
//            }
//            body?.let(::traverse)
        }

        fun move(event: Program.Mouse.MouseEvent) {
            val hover = ElementPseudoClass("hover")

            fun traverse(element: Element) {

                if (event.position in element.screenArea) {
                    if (hover !in element.pseudoClasses) {
                        element.pseudoClasses.add(hover)
                    }
                    element.mouse.moved.onNext(event)
                } else {
                    if (hover in element.pseudoClasses) {
                        element.pseudoClasses.remove(hover)
                    }
                }
                element.children.forEach(::traverse)
            }
            body?.let(::traverse)
        }
    }

    val mouseInput = MouseInput()
    override fun setup(program: Program) {
        contentScale = program.window.scale.x
        fontManager.contentScale = contentScale
        program.mouse.buttonUp.listen { mouseInput.click(it) }
        program.mouse.moved.listen { mouseInput.move(it) }
        program.mouse.scrolled.listen { mouseInput.scroll(it) }
        program.mouse.dragged.listen { mouseInput.drag(it) }
        program.mouse.buttonDown.listen { mouseInput.press(it) }


        program.window.sized.listen { resize(program, it.size.x.toInt(), it.size.y.toInt()) }

        width = program.width
        height = program.height
        renderTarget = renderTarget(program.width, program.height, contentScale) {
            colorBuffer()
        }

        body?.draw?.dirty = true
    }

    var width: Int = 0
    var height: Int = 0

    fun resize(program: Program, width: Int, height: Int) {
        this.width = width
        this.height = height

        body?.draw?.dirty = true

        renderTarget.colorBuffer(0).destroy()
        renderTarget.detachColorBuffers()
        renderTarget.destroy()
        renderTarget = renderTarget(program.width, program.height, contentScale) {
            colorBuffer(program.width, program.height)
        }

        renderTarget.bind()
        program.drawer.background(ColorRGBa.BLACK.opacify(0.0))
        renderTarget.unbind()

        renderTargetCache.forEach { _, u -> u.destroy() }
        renderTargetCache.clear()
    }


    fun drawElement(element: Element, drawer: Drawer, zIndex: Int, zComp: Int) {
        element.draw.dirty = false

        val newZComp =
                element.computedStyle.zIndex.let {
                    when (it) {
                        is ZIndex.Value -> it.value
                        else -> zComp
                    }
                }

        if (element.computedStyle.display != Display.NONE) {
            if (element.computedStyle.overflow == Overflow.Visible) {
                drawer.pushTransforms()
                drawer.pushStyle()
                drawer.translate(element.screenPosition)

                if (newZComp == zIndex) {
                    element.draw(drawer)
                }
                drawer.popStyle()
                drawer.popTransforms()

                element.children.forEach {
                    drawElement(it, drawer, zIndex, newZComp)
                }
            } else {
                val area = element.screenArea
                val rt = renderTargetCache.computeIfAbsent(element) {
                    renderTarget(width, height, contentScale) {
                        colorBuffer()
                        depthBuffer()
                    }
                }

                rt.bind()
                drawer.background(ColorRGBa.BLACK.opacify(0.0))

                drawer.pushProjection()
                drawer.ortho(rt)
                element.children.forEach {
                    drawElement(it, drawer, zIndex, newZComp)
                }
                rt.unbind()
                drawer.popProjection()

                drawer.pushTransforms()
                drawer.pushStyle()
                drawer.translate(element.screenPosition)

                if (newZComp == zIndex) {
                    element.draw(drawer)
                }
                drawer.popStyle()
                drawer.popTransforms()

                drawer.drawStyle.blendMode = BlendMode.OVER
                //drawer.image(rt.colorBuffer(0))
                drawer.image(rt.colorBuffer(0), Rectangle(Vector2(area.x, area.y), area.width, area.height),
                        Rectangle(Vector2(area.x, area.y), area.width, area.height))
            }
        }
    }

    class ProfileData(var hits: Int = 0, var time: Long = 0) {

    }

    val profiles = mutableMapOf<String, ProfileData>()
    fun profile(name: String, f: () -> Unit) {
        val start = System.currentTimeMillis()
        f()
        val end = System.currentTimeMillis()
        val pd = profiles.getOrPut(name) { ProfileData(0, 0L) }
        pd.hits++
        pd.time += (end - start)

        if (pd.hits == 100) {
            println("name:  $name, avg: ${pd.time / pd.hits}ms, ${pd.hits}")
            pd.hits = 0
            pd.time = 0
        }
    }

    override fun afterDraw(drawer: Drawer, program: Program) {

        profile("after draw") {

            if (program.width != renderTarget.width || program.height != renderTarget.height) {
                profile("resize target") {
                    renderTarget.colorBuffer(0).destroy()
                    renderTarget.destroy()
                    renderTarget = renderTarget(program.width, program.height, contentScale) {
                        colorBuffer()
                    }

                    renderTarget.bind()
                    program.drawer.background(ColorRGBa.BLACK.opacify(0.0))
                    renderTarget.unbind()
                }
            }

            val redraw = body?.any {
                it.draw.dirty
            } ?: false

            if (redraw) {
                profile("redraw") {
                    body?.visit {
                        draw.dirty = false
                    }
                    renderTarget.bind()
                    body?.style = StyleSheet()
                    body?.style?.width = program.width.px
                    body?.style?.height = program.height.px


                    body?.let {
                        program.drawer.background(ColorRGBa.BLACK.opacify(0.0))
                        layouter.computeStyles(it)
                        layouter.layout(it)
                        drawElement(it, program.drawer, 0, 0)
                        drawElement(it, program.drawer, 1, 0)
                    }
                    renderTarget.unbind()
                }
            }
            profile("draw image") {
                program.drawer.image(renderTarget.colorBuffer(0), 0.0, 0.0)
            }

        }
    }
}

class ControlManagerBuilder(val controlManager: ControlManager) {
    fun styleSheet(init: StyleSheet.() -> Unit) {
        println("creating a style sheet")
        controlManager.layouter.styleSheets.addAll( StyleSheet().apply { init() }.flatten() )
    }

    fun layout(init: Body.() -> Unit) {
        println("creating the layout")
        val body = Body(controlManager)
        body.init()
        controlManager.body = body
    }

}

fun controlManager(builder:ControlManagerBuilder.()->Unit):ControlManager {
    val cm = ControlManager()
    cm.fontManager.register("default", "file:data/Roboto-Medium.ttf")
    cm.layouter.styleSheets.addAll(defaultStyles())
    val  cmb = ControlManagerBuilder(cm)
    cmb.builder()
    return cm
}

private fun Element.any(function: (Element) -> Boolean): Boolean {
    if (function(this)) {
        return true
    } else {
        children.forEach {
            if (it.any(function)) {
                return true
            }
        }
        return false
    }
}

private fun Element.anyVisible(function: (Element) -> Boolean): Boolean {
    if (computedStyle.display != Display.NONE && function(this)) {
        return true
    }

    if (computedStyle.display != Display.NONE) {
        children.forEach {
            if (it.anyVisible(function)) {
                return true
            }
        }
    }
    return false
}
