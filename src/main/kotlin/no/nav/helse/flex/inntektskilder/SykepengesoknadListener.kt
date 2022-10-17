package no.nav.helse.flex.inntektskilder

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class SykepengesoknadListener(
    private val nyttGenerertSporsmal: NyttGenerertSporsmal,
) {

    @KafkaListener(
        topics = [FLEX_SYKEPENGESOKNAD_TOPIC],
        id = "nytt-generert-sporsmal-listener",
        idIsGroup = false,
        groupId = "nytt-generert-inntektskilde-sporsmal-3"
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {

        val soknad = cr.value().tilSykepengesoknadDTO()

        nyttGenerertSporsmal.finnNyttSporsmal(soknad)

        acknowledgment.acknowledge()
    }
}

fun String.tilSykepengesoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)
