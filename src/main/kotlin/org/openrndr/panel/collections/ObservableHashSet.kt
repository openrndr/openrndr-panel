package org.openrndr.panel.collections

import rx.subjects.PublishSubject
import java.util.*

class ObservableHashSet<E>:HashSet<E>() {

    class ChangeEvent<E>(source:ObservableHashSet<E>, val added:Set<E>, val removed:Set<E>)
    val changed : PublishSubject<ChangeEvent<E>> = PublishSubject.create()

    override fun add(element: E): Boolean {
        return if (super.add(element)) {
            changed.onNext( ChangeEvent(this, setOf(element), emptySet()))
            true
        } else {
            false
        }
    }

    override fun remove(element: E): Boolean {
        return if (super.remove(element)) {
            changed.onNext( ChangeEvent(this, emptySet(), setOf(element)))
            true
        } else {
            false
        }
    }

    override fun clear() {
        val old = this.toSet()
        super.clear()
        changed.onNext(ChangeEvent(this, emptySet(),old))
    }

}