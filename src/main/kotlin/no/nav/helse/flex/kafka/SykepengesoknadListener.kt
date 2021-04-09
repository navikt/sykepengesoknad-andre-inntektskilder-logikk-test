package no.nav.helse.flex.kafka

import no.nav.helse.flex.logger
import no.nav.helse.flex.service.MetrikkService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.event.EventListener
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.event.ConsumerStoppedEvent
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class SykepengesoknadListener(
    private val metrikkService: MetrikkService,
) {

    private val log = logger()

    @KafkaListener(topics = [FLEX_SYKEPENGESOKNAD_TOPIC])
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        try {
            metrikkService.behandleSoknad(cr.value())
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            log.error("Feil ved mottak av record med key: ${cr.key()} offset: ${cr.offset()} partition: ${cr.partition()}", e)
            throw e
        }
    }

    @EventListener
    fun eventHandler(event: ConsumerStoppedEvent) {
        if (event.reason == ConsumerStoppedEvent.Reason.NORMAL) {
            log.debug("Consumer stoppet grunnet NORMAL")
            return
        }
        log.error("Consumer stoppet grunnet ${event.reason}")
        if (event.source is KafkaMessageListenerContainer<*, *> &&
            event.reason == ConsumerStoppedEvent.Reason.AUTH
        ) {
            val container = event.source as KafkaMessageListenerContainer<*, *>
            if (container.groupId == "sykepengesoknad-korrigering-metrikk") {
                log.info("Trying to restart consumer, creds may be rotated")
                container.start()
            }
        }
    }
}
