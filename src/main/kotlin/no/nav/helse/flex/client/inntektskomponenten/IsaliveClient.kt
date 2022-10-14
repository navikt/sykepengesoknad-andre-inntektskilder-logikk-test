package no.nav.helse.flex.client.inntektskomponenten

import no.nav.helse.flex.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URL
import java.time.Instant

@Component
class IsaliveClient(
    @Value("\${FLEX_FSS_PROXY_URL}") private val flexFssProxyUrl: String,

) {

    val log = logger()

    fun ping() {
        val førPing = Instant.now()
        URL("$flexFssProxyUrl/api/ping").content
        Instant.now().let {
            val pingTid = (it.toEpochMilli() - førPing.toEpochMilli()).toInt()
            log.info("Latency mot flex-fss-proxy isALive $pingTid ms")
        }
    }
}
