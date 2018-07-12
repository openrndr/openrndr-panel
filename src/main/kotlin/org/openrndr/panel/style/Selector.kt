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
    override fun accept(element: Element): Boolean = if (element.id != null) {
        element.id.equals(id)
    } else {
        false
    }
}

class ClassSelector(val c: ElementClass) : Selector() {
    override fun accept(element: Element): Boolean = c in element.classes
}

class TypeSelector(val type: ElementType) : Selector() {
    override fun accept(element: Element): Boolean = element.type == type
}

class PseudoClassSelector(val c: ElementPseudoClass) : Selector() {
    override fun accept(element: Element): Boolean = c in element.pseudoClasses
}

class SelectorBuilder {
    var active = CompoundSelector()

    fun id(query: String, pseudo: String? = null): CompoundSelector {
        active.selectors.add(IdentitySelector(query))
        pseudo?.let { active.selectors.add(PseudoClassSelector(ElementPseudoClass(it))) }
        return active
    }

    fun type(query: String, pseudo: String? = null): CompoundSelector {
        active.selectors.add(TypeSelector(ElementType(query)))
        pseudo?.let { active.selectors.add(PseudoClassSelector(ElementPseudoClass(it))) }
        return active
    }

    fun `class`(query: String, pseudo: String? = null): CompoundSelector {
        active.selectors.add(ClassSelector(ElementClass(query)))
        pseudo?.let { active.selectors.add(PseudoClassSelector(ElementPseudoClass(it))) }
        return active
    }
}

infix fun CompoundSelector.followedBy(function: SelectorBuilder.() -> CompoundSelector): CompoundSelector {
    return selector(function).apply {
        previous = Pair(Combinator.LATER_SIBLING, this@followedBy)
    }
}

infix fun CompoundSelector.nextTo(function: SelectorBuilder.() -> CompoundSelector): CompoundSelector {
    return selector(function).apply {
        previous = Pair(Combinator.NEXT_SIBLING, this@nextTo)
    }
}

infix fun CompoundSelector.withChild(function: SelectorBuilder.() -> CompoundSelector): CompoundSelector {
    return selector(function).apply {
        previous = Pair(Combinator.CHILD, this@withChild)
    }
}

infix fun CompoundSelector.withDescendant(function: SelectorBuilder.() -> CompoundSelector): CompoundSelector {
    return selector(function).apply {
        previous = Pair(Combinator.DESCENDANT, this@withDescendant)
    }
}

fun selector(builder: SelectorBuilder.() -> CompoundSelector): CompoundSelector {
    return SelectorBuilder().apply { builder() }.active
}


