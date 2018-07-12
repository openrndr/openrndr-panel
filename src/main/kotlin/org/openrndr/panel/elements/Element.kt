package org.openrndr.panel.elements

import org.openrndr.DropEvent
import org.openrndr.KeyEvent
import org.openrndr.Program
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.panel.collections.ObservableCopyOnWriteArrayList
import org.openrndr.panel.collections.ObservableHashSet
import org.openrndr.panel.style.StyleSheet
import org.openrndr.shape.Rectangle
import rx.subjects.PublishSubject

import java.util.*

data class ElementClass(val name: String)
data class ElementPseudoClass(val name: String)
data class ElementType(val name: String)

class FocusEvent


open class Element(val type: ElementType) {

    var scrollTop = 0.0

    open val widthHint:Double?
    get() { return null }

    class MouseObservables {
        val clicked = PublishSubject.create<Program.Mouse.MouseEvent>()
        val doubleClicked = PublishSubject.create<Program.Mouse.MouseEvent>()
        val entered = PublishSubject.create<Program.Mouse.MouseEvent>()
        val exited = PublishSubject.create<Program.Mouse.MouseEvent>()
        val dragged = PublishSubject.create<Program.Mouse.MouseEvent>()
        val moved = PublishSubject.create<Program.Mouse.MouseEvent>()
        val scrolled = PublishSubject.create<Program.Mouse.MouseEvent>()
        val pressed = PublishSubject.create<Program.Mouse.MouseEvent>()
    }

    class DropObserverables {
        val dropped = PublishSubject.create<DropEvent>()
    }

    val drop = DropObserverables()
    val mouse = MouseObservables()


    class KeyboardObservables {
        val pressed = PublishSubject.create<KeyEvent>()
        val released = PublishSubject.create<KeyEvent>()
        val repeated = PublishSubject.create<KeyEvent>()
        val character = PublishSubject.create<Program.CharacterEvent>()
        val focusGained = PublishSubject.create<FocusEvent>()
        val focusLost = PublishSubject.create<FocusEvent>()
    }

    val keyboard = KeyboardObservables()

    class Layout {
        var screenX = 0.0
        var screenY = 0.0
        var screenWidth = 0.0
        var screenHeight = 0.0
        var growWidth = 0.0
        var growHeight = 0.0
        override fun toString(): String {
            return "Layout(screenX=$screenX, screenY=$screenY, screenWidth=$screenWidth, screenHeight=$screenHeight, growWidth=$growWidth, growHeight=$growHeight)"
        }
    }

    class Draw {
        var dirty = true
    }
    val draw = Draw();
    val layout = Layout()

    class ClassEvent(val source:Element, val `class`:ElementClass)
    class ClassObserverables {
        val classAdded = PublishSubject.create<ClassEvent>()
        val classRemoved = PublishSubject.create<ClassEvent>()
    }

    val classEvents = ClassObserverables()


    var id: String? = null
    val classes: ObservableHashSet<ElementClass> = ObservableHashSet()
    val pseudoClasses: ObservableHashSet<ElementPseudoClass> = ObservableHashSet()

    var parent: Element? = null
    val children: ObservableCopyOnWriteArrayList<Element> = ObservableCopyOnWriteArrayList()
        get() = field

    var computedStyle: StyleSheet = StyleSheet()
    var style: StyleSheet? = null

    init {
        pseudoClasses.changed.subscribe {
            draw.dirty = true }
        classes.changed.subscribe {
            draw.dirty = true
            it.added.forEach {
                classEvents.classAdded.onNext(ClassEvent(this, it))
            }
            it.removed.forEach {
                classEvents.classRemoved.onNext(ClassEvent(this, it))
            }

        }

        children.changed.subscribe {
            draw.dirty = true
        }
    }

    fun root(): Element {
        return parent?.root() ?: this
    }

    open fun append(element: Element) {
        if (element !in children) {
            element.parent = this
            children.add(element)
        }
    }

    fun remove(element: Element) {
        if (element in children) {
            element.parent = null
            children.remove(element)
        }
    }

    open fun draw(drawer: Drawer) {

    }

    fun filter(f: (Element) -> Boolean): List<Element> {
        val result = ArrayList<Element>()
        val stack = Stack<Element>()

        stack.add(this)
        while (!stack.isEmpty()) {
            val node = stack.pop()

            if (f(node)) {
                result.add(node)
                stack.addAll(node.children)
            }
        }
        return result
    }

    fun flatten(): List<Element> {
        val result = ArrayList<Element>()
        val stack = Stack<Element>()

        stack.add(this)
        while (!stack.isEmpty()) {
            val node = stack.pop()

            result.add(node)
            stack.addAll(node.children)
        }
        return result
    }

    fun previousSibling(): Element? {
        parent?.let { p ->
            p.childIndex(this)?.let {
                if (it > 0) {
                    return p.children[it - 1]
                }
            }
        }
        return null
    }

    fun childIndex(element: Element): Int? {
        if (element in children) {
            return children.indexOf(element)
        } else {
            return null
        }
    }

    fun ancestors(): List<Element> {

        var c = this
        val result = ArrayList<Element>()

        while (c.parent != null) {
            c.parent?.let {
                result.add(it)
                c = it
            }
        }
        return result

    }

    fun previous(): Element? {
        return parent?.let { p ->
            val index = p.children.indexOf(this)
            when (index) {
                -1, 0 -> null
                else  -> p.children[index - 1]
            }
        }
    }

    fun next(): Element? {
        return parent?.let { p ->
            val index = p.children.indexOf(this)
            when (index) {
                -1, p.children.size - 1 -> null
                else                    -> p.children[index + 1]
            }
        }

    }

    fun move(steps:Int) {
        parent?.let { p->
            if (steps != 0) {
                val index = p.children.indexOf(this)
                p.children.add(index + steps, this)
                if (steps > 0) {
                    p.children.removeAt(index)
                } else {
                    p.children.removeAt(index+1)
                }
            }
        }
    }

    fun findFirst(element: Element, matches: (Element) -> Boolean): Element? {

        if (matches.invoke(element)) {
            return element
        } else {
            element.children.forEach { c ->
                findFirst(c, matches)?.let { return it }
            }
            return null
        }
    }

    inline fun <reified T> elementWithId(id: String): T? {
        return findFirst(this) { e -> e.id == id && e is T } as T
    }

    val screenPosition: Vector2
        get() = Vector2(layout.screenX, layout.screenY)

    val screenArea: Rectangle
        get() = Rectangle(Vector2(layout.screenX,
                          layout.screenY),
                          layout.screenWidth,
                          layout.screenHeight)


}

fun Element.visit( function:Element.()->Unit) {

    this.function()
    children.forEach { it.visit(function) }

}