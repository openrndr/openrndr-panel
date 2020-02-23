package org.openrndr.panel.elements

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.panel.style.Overflow
import org.openrndr.panel.style.overflow

class Vector2DPad : Element(ElementType("vector-2d-pad")) {
    var expanded = false

    init {
        mouse.pressed.subscribe {
            expanded = !expanded
            it.cancelPropagation()
        }
        
        mouse.scrolled.subscribe {
            computedStyle.let { cs ->
                if (cs.overflow != Overflow.Visible) {
                    scrollTop -= it.rotation.y*10
                    scrollTop = Math.max(0.0, scrollTop)
                    draw.dirty = true
                    it.cancelPropagation()
                }
            }
        }
    }

    override fun draw(drawer: Drawer) {
        if (expanded) {
            drawer.fill = ColorRGBa.RED
            drawer.stroke = null
            drawer.shadeStyle = null
            drawer.rectangle(0.0, 0.0, 100.0, 20.0)
        } else {
            drawer.fill = ColorRGBa.GREEN
            drawer.stroke = null
            drawer.shadeStyle = null
            drawer.rectangle(0.0, 0.0, 100.0, 20.0)
        }
    }

    override fun toString(): String {
        return "Div(id=${id})"
    }
}