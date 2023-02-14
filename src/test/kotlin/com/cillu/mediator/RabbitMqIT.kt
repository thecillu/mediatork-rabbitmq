package com.cillu.mediator

import com.cillu.mediator.integrationevents.domain.FakeIntegrationEvent
import com.cillu.mediator.integrationevents.services.IRepository
import com.cillu.mediator.integrationevents.services.MemoryRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RabbitMqIT: IntegrationBase() {

    @BeforeAll
    fun testInit() {
        /*var builder = EmbeddedRabbitMqConfig.Builder()
            .downloadFolder(File("/tmp/rabbitmq")).envVar(RabbitMqEnvVar.NODENAME,"test")
            .extractionFolder(File("/tmp/rabbitmq/extraction"))
            .rabbitMqServerInitializationTimeoutInMillis(10000)
            .useCachedDownload(true)

        var config = builder.build()
        rabbitMq = EmbeddedRabbitMq(config);
        rabbitMq.start();*/
    }

    @AfterAll
    fun testStop() {
        //rabbitMq.stop()
    }


    @Test
    fun testPublishAndConsume() {
        val mediatorK = getMediatorKwithRabbitMQ(INTEGRATION_EVENTS_CONFIG_FILE_RABBITMQ)
        mediatorK.publish(FakeIntegrationEvent(UUID.randomUUID()))
        Thread.sleep(3000)
        val memoryRepository = mediatorK.getComponent(IRepository::class.java) as MemoryRepository
        assert(memoryRepository.count == 1)
    }
}