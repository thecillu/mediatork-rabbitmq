package com.cillu.mediator

import com.cillu.mediator.domain.Aggregate
import com.cillu.mediator.TestDomainEvent
import java.util.UUID

open class Item private constructor(name: String): Aggregate() {
    lateinit var id: UUID
    var name: String = name

    companion object {
        fun create(name: String): Item {
            var item = Item(name)
            item.id = UUID.randomUUID()
            item.addDomainEvent(TestDomainEvent(UUID.randomUUID(), item))
            return item
        }
    }
}
