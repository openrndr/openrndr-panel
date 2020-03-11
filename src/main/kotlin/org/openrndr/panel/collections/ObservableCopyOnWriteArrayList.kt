package org.openrndr.panel.collections

import io.reactivex.subjects.PublishSubject
import java.util.concurrent.CopyOnWriteArrayList

class ObservableCopyOnWriteArrayList<E> : CopyOnWriteArrayList<E>() {

    val changed: PublishSubject<ObservableCopyOnWriteArrayList<E>> = PublishSubject.create()
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