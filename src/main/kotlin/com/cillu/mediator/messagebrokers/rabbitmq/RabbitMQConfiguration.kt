package com.cillu.mediator.messagebrokers.rabbitmq

data class RabbitMQConfiguration(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val useSslProtocol: Boolean? = false,
    val exchangeName: String,
    val queueName: String,
    val consumerRetryLimit: Int? = 5
)