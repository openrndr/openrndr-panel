package org.openrndr.panel.elements

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.math.Matrix44

class Canvas : Element(ElementType("canvas")) {
    var userDraw: ((Drawer) -> Unit)? = null
    var renderTarget: RenderTarget? = null

    override fun draw(drawer: Drawer) {
        val width = screenArea.width.toInt()
        val height = screenArea.height.toInt()

        println("canvas dimensinos : $width $height")

        if (renderTarget != null) {
            if (renderTarget?.width != width || renderTarget?.height != height) {
                renderTarget?.colorBuffer(0)?.destroy()
                //renderTarget?.de?.destroy()
                renderTarget?.destroy()
                renderTarget = null
            }
        }

        if (screenArea.width >= 1 && screenArea.height >= 1) {
            if (renderTarget == null) {
                renderTarget = renderTarget(screenArea.width.toInt(), screenArea.height.toInt(),1.0) {
                    colorBuffer()
                    depthBuffer()
                }
            }
            val userDrawer = drawer//.copy()

            renderTarget?.let {rt ->
                drawer.isolatedWithTarget(rt) {
                    drawer.view = Matrix44.IDENTITY
                    drawer.background(ColorRGBa.TRANSPARENT)
                    userDrawer.size(screenArea.width.toInt(), screenArea.height.toInt())
                    userDrawer.view
                    userDrawer.ortho(rt)
                    println("userDrawer:  ${userDraw}")
                    userDraw?.invoke(userDrawer)
                }
                drawer.image(rt.colorBuffer(0), 0.0, 0.0)
            }
        }
    }
}