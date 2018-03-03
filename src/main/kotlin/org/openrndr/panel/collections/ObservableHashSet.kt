package org.openrndr.panel.collections

import rx.subjects.PublishSubject
import java.util.*

class ObservableHashSet<E>:HashSet<E>() {

    val changed : PublishSubject<ObservableHashSet<E>> = PublishSubject.create()

    override fun add(element: E): Boolean {
        return if (super.add(element)) {
            changed.onNext(this)
            true
        } else {
            false
        }
    }

    override fun remove(element: E): Boolean {
        return if (super.remove(element)) {
            changed.onNext(this)
            true
        } else {
            false
        }
    }

    override fun clear() {
        super.clear()
        changed.onNext(this)
    }


}