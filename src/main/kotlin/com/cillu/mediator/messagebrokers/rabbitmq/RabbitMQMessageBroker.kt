package com.cillu.mediator.messagebrokers.rabbitmq

import com.cillu.mediator.IMediator
import com.cillu.mediator.integrationevents.IntegrationEvent
import com.cillu.mediator.messagebrokers.IMessageBroker
import com.google.gson.Gson
import com.rabbitmq.client.*
import mu.KotlinLogging
import java.io.IOException



class RabbitMQMessageBroker : IMessageBroker {
    private var host: String
    private var port: Int
    private var username: String
    private var password: String
    private var useSslProtocol: Boolean
    private var exchangeName: String //= "platform_topic"
    private var queueName: String //= "microservice1"
    private var dlqExchangeName: String //= "platform_topic.dlq"
    private var dlqQueueName: String //= "microservice1.dlq"
    private var consumerRetryLimit: Int
    private var channel: Channel
    private var logger = KotlinLogging.logger {}

    private val EXCHANGE_TYPE = "topic"


    internal constructor(
        rabbitMQConfiguration: RabbitMQConfiguration
    ) {
        this.host = rabbitMQConfiguration.host
        this.port = rabbitMQConfiguration.port
        this.username = rabbitMQConfiguration.username
        this.password = rabbitMQConfiguration.password
        this.useSslProtocol = rabbitMQConfiguration.useSslProtocol!!
        this.exchangeName = rabbitMQConfiguration.exchangeName
        this.queueName = rabbitMQConfiguration.queueName
        this.dlqExchangeName = "${rabbitMQConfiguration.exchangeName}.dlq"
        this.dlqQueueName = "${rabbitMQConfiguration.queueName}.dlq"
        this.consumerRetryLimit = rabbitMQConfiguration.consumerRetryLimit!!
        val factory = ConnectionFactory()
        factory.host = this.host
        factory.port = this.port
        factory.username = this.username
        factory.password = this.password
        if (this.useSslProtocol) factory.useSslProtocol()
        val connection = factory.newConnection()
        channel = connection.createChannel()
        channel.exchangeDeclare(exchangeName, EXCHANGE_TYPE, true)
        channel.exchangeDeclare(dlqExchangeName, EXCHANGE_TYPE, true)
        var args: HashMap<String, Any> = HashMap()
        args["x-dead-letter-exchange"] = dlqExchangeName
        args["x-queue-type"] = "quorum"
        args["delivery-limit"] = consumerRetryLimit
        channel.queueDeclare(queueName, true, false, false, args)
        channel.queueDeclare(dlqQueueName, true, false, false, null)
    }

    override fun bind(integrationEventName: String) {
        logger.info("Binding routingKey = $integrationEventName on Queue $queueName and Exchange $exchangeName")
        channel.queueBind(queueName, exchangeName, integrationEventName)
        logger.info("Binding routingKey = $integrationEventName on Queue $dlqQueueName and Exchange $dlqExchangeName")
        channel.queueBind(dlqQueueName, dlqExchangeName, integrationEventName)
    }

    override fun consume(mediator: IMediator) {
        logger.info("Creating ServiceBus Consumer")
        val consumer: Consumer = object : DefaultConsumer(channel) {
            @Throws(IOException::class)
            override fun handleDelivery(
                consumerTag: String?,
                envelope: Envelope?,
                properties: AMQP.BasicProperties?,
                body: ByteArray?
            ) {
                val deliveryTag = envelope!!.deliveryTag
                val integrationEventName: String = envelope.routingKey ?: ""
                try {
                    val message = String(body!!)
                    logger.info("Received $integrationEventName with payload: $message")
                    process(mediator, integrationEventName, message)
                    channel.basicAck(deliveryTag, false)
                    logger.info("ACK sent for $integrationEventName")
                } catch (e: Exception) {
                    e.printStackTrace()
                    channel.basicNack(deliveryTag, false, true)
                    logger.info("NACK sent for $integrationEventName")
                }
            }
        }
        logger.info("Created ServiceBus Consumer")
        logger.info("Listening for IntegrationEvent on queue $queueName....")
        channel.basicConsume(queueName, false, consumer)
    }

    private fun process(mediator: IMediator, integrationEventName: String, message: String) {
        var integrationEvent = Gson().fromJson(message, Class.forName(integrationEventName))
        mediator.process(integrationEvent as IntegrationEvent)
    }

    override fun publish(integrationEvent: IntegrationEvent) {
        var json = Gson().toJson(integrationEvent)
        channel.basicPublish(
            exchangeName,
            integrationEvent::class.java.name,
            null,
            json.toByteArray(),
        )
    }
}