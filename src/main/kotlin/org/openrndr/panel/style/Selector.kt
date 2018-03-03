package org.openrndr.panel.style

import org.openrndr.panel.elements.Element
import org.openrndr.panel.elements.ElementClass
import org.openrndr.panel.elements.ElementPseudoClass
import org.openrndr.panel.elements.ElementType

data class SelectorPrecedence(var inlineStyle: Int = 0, var id: Int = 0, var classOrAttribute: Int = 0, var type: Int = 0)

abstract class Selector {
    abstract fun accept(element: Element): Boolean
}

class CompoundSelector {
    var previous: Pair<Combinator, CompoundSelector>?
    var selectors: MutableList<Selector>

    constructor() {
        previous = null
        selectors = ArrayList()
    }

    constructor(previous: Pair<Combinator, CompoundSelector>?, selectors: List<Selector>) {
        this.previous = previous
        this.selectors = ArrayList()
        selectors.forEach { this.selectors.add(it) }
    }

    fun precedence(p: SelectorPrecedence = SelectorPrecedence()): SelectorPrecedence {

        selectors.forEach {
            when (it) {
                is IdentitySelector -> p.id++
                is ClassSelector, is PseudoClassSelector -> p.classOrAttribute++
                is TypeSelector -> p.type++
                else -> {
                }
            }
        }
        var r = p
        previous?.let {
            r = it.second.precedence(p)
        }
        return r
    }
}

enum class Combinator {
    CHILD, DESCENDANT, NEXT_SIBLING, LATER_SIBLING
}

class IdentitySelector(val id: String) : Selector() {
    override fun accept(element: Element): Boolean {
        if (element.id != null) {
            return element.id.equals(id)
        } else {
            return false
        }
    }
}

class ClassSelector(val c: ElementClass) : Selector() {
    override fun accept(element: Element): Boolean {
        return c in element.classes
    }
}

class TypeSelector(val type: ElementType) : Selector() {
    override fun accept(element: Element): Boolean {
        return element.type == type
    }
}

class PseudoClassSelector(val c: ElementPseudoClass) : Selector() {
    override fun accept(element: Element): Boolean {
        return c in element.pseudoClasses
    }

}

fun selector(id: String? = null, class_: String? = null, type: String? = null): CompoundSelector {
    val cs = CompoundSelector()
    id?.let { cs.id(it) }
    type?.let { cs.type(it) }
    class_?.let { cs.class_(it) }
    return cs
}

fun select(id: String? = null, class_: String? = null, type: String? = null, pseudoClass:String? = null, init: CompoundSelector.() -> Unit): CompoundSelector {

    val cs = CompoundSelector()
    id?.let { cs.id(it) }
    type?.let { cs.type(it) }
    class_?.let { cs.class_(it) }
    pseudoClass?.let { cs.pseudoClass(it)}

    cs.init()
    return cs
}


infix fun CompoundSelector.withChild(selector: CompoundSelector): CompoundSelector {
    return CompoundSelector(
            previous = Pair(Combinator.CHILD, this),
            selectors = selector.selectors)
}

infix fun CompoundSelector.withDescendant(selector: CompoundSelector): CompoundSelector {
    return CompoundSelector(
            previous = Pair(Combinator.DESCENDANT, this),
            selectors = selector.selectors)
}

infix fun CompoundSelector.followedBy(selector: CompoundSelector): CompoundSelector {
    return CompoundSelector(
            previous = Pair(Combinator.LATER_SIBLING, this),
            selectors = selector.selectors)
}

fun CompoundSelector.id(s: String) {
    selectors.add(IdentitySelector(s))
}

fun CompoundSelector.type(s: String) {
    selectors.add(TypeSelector(ElementType(s)))
}

fun CompoundSelector.class_(s: String) {
    selectors.add(ClassSelector(ElementClass(s)))
}

fun CompoundSelector.pseudoClass(s:String) {
    selectors.add(PseudoClassSelector(ElementPseudoClass(s)))
}

