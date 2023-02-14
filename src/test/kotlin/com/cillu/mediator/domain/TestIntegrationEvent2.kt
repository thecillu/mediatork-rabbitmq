package com.cillu.mediator.domain

import com.cillu.mediator.Item
import com.cillu.mediator.integrationevents.IntegrationEvent
import java.util.*

class TestIntegrationEvent2(idEvent: UUID, item: Item): IntegrationEvent(idEvent) {
    val item: Item = item
}


