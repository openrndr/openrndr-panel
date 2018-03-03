package org.openrndr.panel.layout

import org.openrndr.math.Vector2
import org.openrndr.panel.elements.Element
import org.openrndr.panel.elements.TextNode
import org.openrndr.panel.style.*
import org.openrndr.shape.Rectangle
import java.util.*
import kotlin.comparisons.compareBy

class Layouter {
    val styleSheets = ArrayList<StyleSheet>()

    val blockLike = setOf(Display.BLOCK, Display.FLEX)
    val manualPosition = setOf(Position.FIXED, Position.ABSOLUTE)
    fun positionChildren(element: Element): Rectangle {

        return element.computedStyle.let { cs ->
            var y = element.layout.screenY - element.scrollTop

            when (cs.display) {
                Display.FLEX -> {

                    when (cs.flexDirection) {

                        FlexDirection.Row -> {
                            var maxHeight = 0.0
                            var x = element.layout.screenX

                            val totalWidth = element.children.filter { it.computedStyle.display in blockLike && it.computedStyle.position !in manualPosition }.map { width(it) }.sum()
                            val remainder = (element.layout.screenWidth-totalWidth)
                            val totalGrow = element.children.filter { it.computedStyle.display in blockLike && it.computedStyle.position !in manualPosition }.map { (it.computedStyle.flexGrow as FlexGrow.Ratio).value }.sum()

                            element.children.forEach {

                                val elementGrow = (it.computedStyle.flexGrow as FlexGrow.Ratio).value
                                val growWidth = if (totalGrow > 0) (elementGrow / totalGrow) * remainder else 0.0

                                it.layout.screenY = y + ((it.computedStyle.marginTop as? LinearDimension.PX)?.value ?: 0.0)
                                it.layout.screenX = x + ((it.computedStyle.marginLeft as? LinearDimension.PX)?.value ?: 0.0)

                                it.layout.growWidth = growWidth
                                x += width(it)+ growWidth
                                maxHeight = Math.max(height(it), maxHeight)
                            }
                            Rectangle(Vector2(x, y), x - element.layout.screenX, maxHeight)
                        }
                        FlexDirection.Column -> {
                            var maxWidth = 0.0
                            var ly = element.layout.screenY
                            var lx = element.layout.screenX

                            val totalHeight = element.children
                                    .filter { it.computedStyle.display in blockLike && it.computedStyle.position !in manualPosition }
                                    .sumByDouble { height(it) }
                            val remainder = (element.layout.screenHeight - totalHeight)
                            val totalGrow = element.children
                                    .filter { it.computedStyle.display in blockLike && it.computedStyle.position !in manualPosition }
                                    .sumByDouble { (it.computedStyle.flexGrow as FlexGrow.Ratio).value }

                            element.children.forEach {
                                val elementGrow = (it.computedStyle.flexGrow as FlexGrow.Ratio).value
                                val growHeight = if (totalGrow > 0) (elementGrow / totalGrow) * remainder else 0.0

                                it.layout.screenY = ly + ((it.computedStyle.marginTop as? LinearDimension.PX)?.value ?: 0.0)
                                it.layout.screenX = lx + ((it.computedStyle.marginLeft as? LinearDimension.PX)?.value ?: 0.0)

                                it.layout.growHeight = growHeight
                                ly += height(it)+ growHeight
                                maxWidth = Math.max(height(it), maxWidth)

                            }

                            Rectangle(Vector2(lx, ly), maxWidth, ly - element.layout.screenY)
                        }
                        else              -> Rectangle(Vector2(element.layout.screenX, element.layout.screenY), 0.0, 0.0)
                    }
                }
                else         -> {
                    var x = element.layout.screenX
                    var maxWidth = 0.0
                    element.children.forEach {
                        if (it.computedStyle.display in blockLike && it.computedStyle.position !in manualPosition) {
                            it.layout.screenY = y + ((it.computedStyle.marginTop as? LinearDimension.PX)?.value ?: 0.0)
                            it.layout.screenX = x + ((it.computedStyle.marginLeft as? LinearDimension.PX)?.value ?: 0.0)
                            maxWidth = Math.max(0.0, width(it))
                            y += height(it)
                        }
                        else if (it.computedStyle.position == Position.ABSOLUTE) {
                            it.layout.screenX = element.layout.screenX + ((it.computedStyle.left as? LinearDimension.PX)?.value?:0.0)
                            it.layout.screenY = element.layout.screenY + ((it.computedStyle.top as? LinearDimension.PX)?.value?:0.0)
                        }
                    }
                    Rectangle(Vector2(element.layout.screenX, element.layout.screenY), maxWidth, y - element.layout.screenY)
                }
            }
        }
    }

    fun computeStyles(element: Element) {
        val matcher = Matcher()

        if (element is TextNode) {
            element.computedStyle = element.parent?.computedStyle?.cascadeOnto(StyleSheet()) ?: StyleSheet()
        } else {
            element.computedStyle =
                    styleSheets
                            .filter {
                                it.selector?.let {
                                    matcher.matches(it, element)
                                } ?: false
                            }
                            .sortedWith(compareBy({ it.precedence.component1() },
                                                  { it.precedence.component2() },
                                                  { it.precedence.component3() },
                                                  { it.precedence.component4() }))
                            .fold(StyleSheet(), { a, b -> a.cascadeOnto(b) })

            element.style?.let {
                element.computedStyle = it.cascadeOnto(element.computedStyle)
            }
        }
        element.computedStyle.let { cs ->

            element.parent?.let { p ->
                cs.properties.forEach { k, v ->
                    if ((v.value as? PropertyValue)?.inherit == true) {
                        cs.properties.put(k, p.computedStyle.getProperty(k) ?: v)
                    }
                }
                PropertyBehaviours.behaviours.forEach { k, v ->
                    if (v.inheritance == PropertyInheritance.INHERIT && k !in cs.properties) {
                        if (k in p.computedStyle.properties) {
                            cs.properties.put(k, p.computedStyle.getProperty(k)!!)
                        }
                    }
                }
            }
        }

        element.children.forEach { computeStyles(it) }
    }

    fun margin(element: Element, f:(StyleSheet)->LinearDimension): Double {
        val value = f(element.computedStyle)
        return when (value) {
            is LinearDimension.PX -> value.value
            else  -> 0.0
        }
    }

    fun marginTop(element:Element) = margin(element, StyleSheet::marginTop)
    fun marginBottom(element:Element) = margin(element, StyleSheet::marginBottom)
    fun marginLeft(element:Element) = margin(element, StyleSheet::marginLeft)
    fun marginRight(element:Element) = margin(element, StyleSheet::marginRight)



    fun height(element: Element, includeMargins: Boolean = true): Double {

        if (element.computedStyle.display == Display.NONE) {
            return 0.0
        }

        if (element is TextNode) {
            return element.sizeHint().height + if (includeMargins) marginBottom(element) + marginTop(element)  else 0.0
        }


        return element.computedStyle.let {
            it.height.let {
                when (it) {
                    is LinearDimension.PX -> it.value
                    is LinearDimension.Percent -> (element.parent?.layout?.screenHeight ?: 0.0) * (it.value / 100.0) - 1.0*(marginBottom(element) + marginTop(element))
                    is LinearDimension.Auto -> {
                        positionChildren(element).height
                    }

                    else    -> {
                        throw RuntimeException("not supported")
                    }
                }
            } + if (includeMargins) ((it.marginTop as? LinearDimension.PX)?.value ?: 0.0) + ((it.marginBottom as? LinearDimension.PX)?.value ?: 0.0) else 0.0
        }
    }

    fun width(element: Element, includeMargins: Boolean = true): Double = element.computedStyle.let {
        if (element.computedStyle.display == Display.NONE) {
            return 0.0
        }

        it.width.let {
            when (it) {
                is LinearDimension.PX -> it.value
                is LinearDimension.Percent -> (element.parent?.layout?.screenWidth ?: 0.0) * (it.value / 100.0) - 1.0*(marginLeft(element) + marginRight(element))
                is LinearDimension.Auto -> positionChildren(element).width
                else       -> throw RuntimeException("not supported")
            }
        } + if (includeMargins) marginLeft(element) + marginRight(element) else 0.0
    }


    fun layout(element: Element) {
        element.computedStyle.let { cs ->

            cs.display.let { if (it == Display.NONE) return }

            when (cs.position) {
                Position.FIXED -> {
                    element.layout.screenX = (cs.left as? LinearDimension.PX)?.value ?: 0.0
                    element.layout.screenY = (cs.top as? LinearDimension.PX)?.value ?: 0.0
                }
                else -> {}
            }

                element.layout.screenWidth = width(element, includeMargins = false)
                element.layout.screenHeight = height(element, includeMargins = false)

            element.layout.screenWidth += element.layout.growWidth
            element.layout.screenHeight += element.layout.growHeight

            positionChildren(element)

        }
        element.children.forEach { layout(it) }
    }

}