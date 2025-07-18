package com.emarsys.core.collections


//TODO: Received concurrent modification exception, needs investigation
suspend fun <Element>MutableList<Element>.dequeue(backward: Boolean = false, action: suspend (element: Element) -> (Unit)) {
    if (backward) {
        val iterator = this.listIterator(this.size)
        while (iterator.hasPrevious()) {
            action.invoke(iterator.previous())
            iterator.remove()
        }
    } else {
        val iterator = this.listIterator()
        while (iterator.hasNext()) {
            action.invoke(iterator.next())
            iterator.remove()
        }
    }
}
