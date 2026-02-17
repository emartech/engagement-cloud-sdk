package com.sap.ec.core.collections

import com.sap.ec.core.storage.StorageApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer

fun <Element>persistentListOf(id: String, storage: StorageApi, elementSerializer: KSerializer<Element>, vararg elements: Element): MutableList<Element> {
    return PersistentList(id, storage, elementSerializer, elements.toList())
}

class PersistentList<Element>(
    private val id: String,
    private val storage: StorageApi,
    elementSerializer: KSerializer<Element>): MutableList<Element> {

    constructor(
        id: String,
        storage: StorageApi,
        elementSerializer: KSerializer<Element>,
        elements: List<Element>) : this(id, storage, elementSerializer) {
        this.elements = elements.toMutableList()
        persist()
    }

    private val elementsSerializer = ListSerializer(elementSerializer)

    private var elements: MutableList<Element> =
        storage.get(id, elementsSerializer)?.toMutableList() ?: mutableListOf()

    override val size: Int
        get() = elements.size

    override fun clear() {
        elements.clear()
        persist()
    }

    override fun addAll(elements: Collection<Element>): Boolean {
        val result = this.elements.addAll(elements)
        persist()
        return result
    }

    override fun addAll(index: Int, elements: Collection<Element>): Boolean {
        val result = this.elements.addAll(index, elements)
        persist()
        return result
    }

    override fun add(index: Int, element: Element) {
        this.elements.add(index, element)
        persist()
    }

    override fun add(element: Element): Boolean {
        val result = this.elements.add(element)
        persist()
        return result
    }

    override fun get(index: Int): Element {
        return elements[index]
    }

    override fun isEmpty(): Boolean {
        return elements.isEmpty()
    }

    override fun iterator(): MutableIterator<Element> {
        return elements.iterator()
    }

    override fun listIterator(): MutableListIterator<Element> {
        return elements.listIterator()
    }

    override fun listIterator(index: Int): MutableListIterator<Element> {
        return elements.listIterator(index)
    }

    override fun removeAt(index: Int): Element {
        val result = elements.removeAt(index)
        persist()
        return result
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Element> {
        return elements.subList(fromIndex, toIndex)
    }

    override fun set(index: Int, element: Element): Element {
        val result = elements.set(index, element)
        persist()
        return result
    }

    override fun retainAll(elements: Collection<Element>): Boolean {
        val result = this.elements.retainAll(elements)
        persist()
        return result
    }

    override fun removeAll(elements: Collection<Element>): Boolean {
        val result = this.elements.removeAll(elements)
        persist()
        return result
    }

    override fun remove(element: Element): Boolean {
        val result = elements.remove(element)
        persist()
        return result
    }

    override fun lastIndexOf(element: Element): Int {
        return elements.lastIndexOf(element)
    }

    override fun indexOf(element: Element): Int {
        return elements.indexOf(element)
    }

    override fun containsAll(elements: Collection<Element>): Boolean {
        return this.elements.containsAll(elements)
    }

    override fun contains(element: Element): Boolean {
        return elements.contains(element)
    }

    private fun persist() {
        storage.put(id, elementsSerializer, elements)
    }

}
