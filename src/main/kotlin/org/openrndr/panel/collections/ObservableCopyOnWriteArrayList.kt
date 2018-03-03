package org.openrndr.panel.collections


import rx.subjects.PublishSubject
import java.util.concurrent.CopyOnWriteArrayList

class ObservableCopyOnWriteArrayList<E> : CopyOnWriteArrayList<E>() {

    //val changed = PublishSubject<ObservableCopyOnWriteArrayList<E>>()
    val changed : PublishSubject<ObservableCopyOnWriteArrayList<E>> =
    PublishSubject.create()
    override fun add(element: E): Boolean {
        if (super.add(element)) {
            changed.onNext(this)
            return true
        } else {
            return false
        }
    }

    override fun remove(element: E): Boolean {
        if (super.remove(element)) {
            changed.onNext(this)
            return true
        } else {
            return false
        }
    }

    override fun clear() {
        super.clear()
        changed.onNext(this)
    }

}