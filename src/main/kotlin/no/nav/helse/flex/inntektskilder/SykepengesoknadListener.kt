package no.nav.helse.flex.inntektskilder

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.logger
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
@Profile("test")
class SykepengesoknadListener(
    private val nyttGenerertSporsmal: NyttGenerertSporsmal,
) {

    private val log = logger()

    @KafkaListener(
        topics = [FLEX_SYKEPENGESOKNAD_TOPIC],
        id = "prometheus-metrikker-listener",
        idIsGroup = false,
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {

        val soknad = cr.value().tilSykepengesoknadDTO()

        nyttGenerertSporsmal.finnNyttSporsmal(soknad)

        acknowledgment.acknowledge()
    }
}

fun String.tilSykepengesoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)