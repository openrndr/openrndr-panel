package org.openrndr.panel

import org.openrndr.*
import org.openrndr.binpack.IntPacker
import org.openrndr.binpack.PackNode
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.panel.elements.*
import org.openrndr.panel.layout.Layouter
import org.openrndr.panel.style.*
import org.openrndr.shape.IntRectangle
import org.openrndr.shape.Rectangle

//class SurfaceCache(val width: Int, val height: Int, val contentScale:Double) {
//    val cache = renderTarget(width, height, contentScale) {
//        colorBuffer()
//        depthBuffer()
//    }
//
//    private val atlas = mutableMapOf<Element, IntRectangle>()
//
//    private var root: PackNode = PackNode(IntRectangle(0, 0, width, height))
//    val packer = IntPacker()
//
//    fun flush() {
//        atlas.clear()
//        root = PackNode(IntRectangle(0, 0, width, height))
//    }
//
//    fun drawCached(drawer: Drawer, element: Element, f: () -> Unit): IntRectangle {
//        val rectangle = atlas.getOrPut(element) {
//            val r = packer.insert(root, IntRectangle(0, 0, Math.ceil(element.layout.screenWidth).toInt(),
//                    Math.ceil(element.layout.screenHeight).toInt()))?.area?:throw RuntimeException("bork")
//            draw(drawer, r, f)
//            r
//        }
//        if (element.draw.dirty) {
//            draw(drawer, rectangle, f)
//        }
//        drawer.ortho()
//        drawer.isolated {
//            drawer.model = Matrix44.IDENTITY
//            drawer.image(cache.colorBuffer(0), Rectangle(rectangle.corner.x * 1.0, rectangle.corner.y * 1.0, rectangle.width * 1.0, rectangle.height * 1.0),
//                    element.screenArea)
//        }
//        return rectangle
//    }
//
//    fun draw(drawer: Drawer, rectangle: IntRectangle, f: () -> Unit) {
//        drawer.isolatedWithTarget(cache) {
//            drawer.ortho(cache)
//            drawer.drawStyle.blendMode = BlendMode.REPLACE
//            drawer.drawStyle.fill = ColorRGBa.BLACK.opacify(0.0)
//            drawer.drawStyle.stroke = null
//            drawer.view = Matrix44.IDENTITY
//            drawer.model = Matrix44.IDENTITY
//            drawer.rectangle(Rectangle(rectangle.x * 1.0, rectangle.y * 1.0, rectangle.width * 1.0, rectangle.height * 1.0))
//
//            drawer.drawStyle.blendMode = BlendMode.OVER
//            drawer.drawStyle.clip = Rectangle(rectangle.x * 1.0, rectangle.y * 1.0, rectangle.width * 1.0, rectangle.height * 1.0)
//            drawer.view = Matrix44.IDENTITY
//            drawer.model = org.openrndr.math.transforms.translate(rectangle.x * 1.0, rectangle.y * 1.0, 0.0)
//            f()
//        }
//    }
//}

class ControlManager : Extension {
    var body: Element? = null
    val layouter = Layouter()
    val fontManager = FontManager()
    lateinit var window: Program.Window
    private val renderTargetCache = HashMap<Element, RenderTarget>()

    lateinit var program: Program
    override var enabled: Boolean = true

    var contentScale = 1.0
    lateinit var renderTarget: RenderTarget

    inner class DropInput {
        var target: Element? = null
        fun drop(event: DropEvent) {
            target?.drop?.dropped?.onNext(event)
        }
    }

    fun requestScrollInput(element: Element) {

    }

    val dropInput = DropInput()

    inner class KeyboardInput {
        var target: Element? = null
            set(value) {
                field?.keyboard?.focusLost?.onNext(FocusEvent())
                value?.keyboard?.focusGained?.onNext(FocusEvent())
                field = value
            }

        fun press(event: KeyEvent) {
            target?.let {
                var current: Element? = it
                while (current != null) {
                    if (!event.propagationCancelled) {
                        current.keyboard.pressed.onNext(event)
                    }
                    current = current.parent
                }
                checkForManualRedraw()
            }
        }

        fun release(event: KeyEvent) {
            target?.keyboard?.released?.onNext(event)
            if (target != null) {
                checkForManualRedraw()
            }
        }

        fun repeat(event: KeyEvent) {
            target?.keyboard?.repeated?.onNext(event)
            if (target != null) {
                checkForManualRedraw()
            }
        }

        fun character(event: Program.CharacterEvent) {
            target?.keyboard?.character?.onNext(event)
            if (target != null) {
                checkForManualRedraw()
            }
        }
    }

    val keyboardInput = KeyboardInput()

    inner class MouseInput {
        var dragTarget: Element? = null
        var clickTarget: Element? = null
        var lastClick = System.currentTimeMillis()

        fun scroll(event: MouseEvent) {
            fun traverse(element: Element) {
                element.children.forEach(::traverse)
                if (!event.propagationCancelled) {
                    if (event.position in element.screenArea && element.computedStyle.display != Display.NONE) {
                        element.mouse.scrolled.onNext(event)
                        if (event.propagationCancelled) {
                            keyboardInput.target = element
                        }
                    }
                }
            }
            body?.let(::traverse)
            checkForManualRedraw()
        }

        fun click(event: MouseEvent) {
            dragTarget = null
            val ct = System.currentTimeMillis()
            if (ct - lastClick > 500) {
                if (clickTarget != null) {
                    clickTarget?.mouse?.clicked?.onNext(event)
                }
            } else {
                if (clickTarget != null) {
                    clickTarget?.mouse?.doubleClicked?.onNext(event)
                }
            }
            lastClick = ct
            checkForManualRedraw()
        }

        fun press(event: MouseEvent) {
            val candidates = mutableListOf<Pair<Element, Int>>()
            fun traverse(element: Element, depth: Int = 0) {
                if (element.computedStyle.display != Display.NONE) {
                    element.children.forEach { traverse(it, depth + 1) }
                }
                if (!event.propagationCancelled && event.position in element.screenArea && element.computedStyle.display != Display.NONE) {
                    candidates.add(Pair(element, depth))
                }
            }

            body?.let { traverse(it) }
            //candidates.sortByDescending { it.second }
            candidates.sortWith(compareBy({-it.first.layout.zIndex},{-it.second}))
            for (c in candidates) {
                if (!event.propagationCancelled) {
                    c.first.mouse.pressed.onNext(event)
                    if (event.propagationCancelled) {
                        dragTarget = c.first
                        clickTarget = c.first
                        keyboardInput.target = c.first
                    }
                }
            }
            checkForManualRedraw()
        }

        fun release(event: MouseEvent) {

        }

        fun drag(event: MouseEvent) {
            dragTarget?.mouse?.dragged?.onNext(event)

            if (event.propagationCancelled) {
                clickTarget = null
            }
            checkForManualRedraw()
        }

        val insideElements = mutableSetOf<Element>()
        fun move(event: MouseEvent) {
            val hover = ElementPseudoClass("hover")
            val toRemove = insideElements.filter { (event.position !in it.screenArea) }

            toRemove.forEach {
                it.mouse.exited.onNext(MouseEvent(event.position, Vector2.ZERO, Vector2.ZERO, MouseEventType.MOVED, MouseButton.NONE, event.modifiers, false))
            }

            insideElements.removeAll(toRemove)

            fun traverse(element: Element) {
                if (event.position in element.screenArea) {
                    if (element !in insideElements) {
                        element.mouse.entered.onNext(event)
                    }
                    insideElements.add(element)
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
            checkForManualRedraw()
        }
    }

    fun checkForManualRedraw() {
        if (window.presentationMode == PresentationMode.MANUAL) {
            val redraw = body?.any {
                it.draw.dirty
            } ?: false
            if (redraw) {
                window.requestDraw()
            }
        }
    }

    val mouseInput = MouseInput()
    override fun setup(program: Program) {
        this.program = program

        contentScale = program.window.scale.x
        window = program.window

        //surfaceCache = SurfaceCache(4096, 4096, contentScale)
        fontManager.contentScale = contentScale
        program.mouse.buttonUp.listen { mouseInput.release(it) }
        program.mouse.buttonUp.listen { mouseInput.click(it) }
        program.mouse.moved.listen { mouseInput.move(it) }
        program.mouse.scrolled.listen { mouseInput.scroll(it) }
        program.mouse.dragged.listen { mouseInput.drag(it) }
        program.mouse.buttonDown.listen { mouseInput.press(it) }

        program.keyboard.keyDown.listen { keyboardInput.press(it) }
        program.keyboard.keyUp.listen { keyboardInput.release(it) }
        program.keyboard.keyRepeat.listen { keyboardInput.repeat(it) }
        program.keyboard.character.listen { keyboardInput.character(it) }

        program.window.drop.listen { dropInput.drop(it) }
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

        if (renderTarget.colorBuffers.isNotEmpty()) {
            renderTarget.colorBuffer(0).destroy()
            renderTarget.depthBuffer?.destroy()
            renderTarget.detachColorBuffers()
            renderTarget.detachDepthBuffer()
            renderTarget.destroy()
        } else {
            println("that is strange. no color buffers")
        }

        renderTarget = renderTarget(program.width, program.height, contentScale) {
            colorBuffer()
            depthBuffer()
        }

        renderTarget.bind()
        program.drawer.background(ColorRGBa.BLACK.opacify(0.0))
        renderTarget.unbind()

        renderTargetCache.forEach { _, u -> u.destroy() }
        renderTargetCache.clear()
    }

    private fun drawElement(element: Element, drawer: Drawer, zIndex: Int, zComp: Int) {
        val newZComp =
                element.computedStyle.zIndex.let {
                    when (it) {
                        is ZIndex.Value -> it.value
                        else -> zComp
                    }
                }

        if (element.computedStyle.display != Display.NONE) {
            if (element.computedStyle.overflow == Overflow.Visible) {
                drawer.isolated {
                    drawer.translate(element.screenPosition)
                    element.draw(drawer)
//                    if (newZComp == zIndex) {
//                        surfaceCache.drawCached(drawer, element) {
//                            element.draw(drawer)
//                        }
//
//                    }
                }
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
        element.draw.dirty = false

    }

    class ProfileData(var hits: Int = 0, var time: Long = 0);

    val profiles = mutableMapOf<String, ProfileData>()
    fun profile(name: String, f: () -> Unit) {
        val start = System.currentTimeMillis()
        f()
        val end = System.currentTimeMillis()
        val pd = profiles.getOrPut(name) { ProfileData(0, 0L) }
        pd.hits++
        pd.time += (end - start)

        if (pd.hits == 100) {
            //println("name:  $name, avg: ${pd.time / pd.hits}ms, ${pd.hits}")
            pd.hits = 0
            pd.time = 0
        }
    }

    var drawCount = 0
    override fun afterDraw(drawer: Drawer, program: Program) {
        if (program.width > 0 && program.height > 0) {
            profile("after draw") {

                if (program.width != renderTarget.width || program.height != renderTarget.height) {
                    profile("resize target") {
                        body?.draw?.dirty = true

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
                    drawer.ortho()
                    drawer.view = Matrix44.IDENTITY
                    drawer.reset()

                    profile("redraw") {
                        renderTarget.bind()
                        body?.style = StyleSheet(CompoundSelector())
                        body?.style?.width = program.width.px
                        body?.style?.height = program.height.px

                        body?.let {
                            program.drawer.background(ColorRGBa.BLACK.opacify(0.0))
                            layouter.computeStyles(it)
                            layouter.layout(it)
                            drawElement(it, program.drawer, 0, 0)
                            drawElement(it, program.drawer, 1, 0)
                            drawElement(it, program.drawer, 1000, 0)
                        }
                        renderTarget.unbind()
                    }
                }
                body?.visit {
                    draw.dirty = false
                }

                profile("draw image") {
                    drawer.size(program.width, program.height)
                    drawer.ortho()
                    drawer.view = Matrix44.IDENTITY
                    drawer.reset()
                    program.drawer.image(renderTarget.colorBuffer(0), 0.0, 0.0)

                }
                drawCount++

            }
        }
    }
}

class ControlManagerBuilder(val controlManager: ControlManager) {
    fun styleSheet(selector:CompoundSelector, init: StyleSheet.() -> Unit) : StyleSheet {
        val styleSheet = StyleSheet(selector).apply { init() }
        controlManager.layouter.styleSheets.addAll(styleSheet.flatten())
        return styleSheet
    }

    fun styleSheets(styleSheets: List<StyleSheet>) {
        controlManager.layouter.styleSheets.addAll(styleSheets.flatMap { it.flatten() })
    }

    fun layout(init: Body.() -> Unit) {
        val body = Body(controlManager)
        body.init()
        controlManager.body = body
    }
}

fun controlManager(builder: ControlManagerBuilder.() -> Unit): ControlManager {
    val cm = ControlManager()
    cm.fontManager.register("default", resourceUrl("/fonts/Roboto-Regular.ttf"))
    cm.layouter.styleSheets.addAll(defaultStyles().flatMap { it.flatten() })
    val cmb = ControlManagerBuilder(cm)
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