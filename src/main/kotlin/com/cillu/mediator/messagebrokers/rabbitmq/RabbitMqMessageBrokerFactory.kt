package com.cillu.mediator.messagebrokers.rabbitmq

import com.cillu.mediator.messagebrokers.IMessageBroker

class RabbitMqMessageBrokerFactory private constructor() {

    companion object {
        fun build(rabbitMQConfiguration: RabbitMQConfiguration): IMessageBroker {
            return RabbitMQMessageBroker( rabbitMQConfiguration )
        }
    }

}