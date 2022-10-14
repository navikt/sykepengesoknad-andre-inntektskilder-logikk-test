package no.nav.helse.flex.client.inntektskomponenten

import no.nav.helse.flex.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.net.URI
import java.time.Instant

@Component
class IsaliveClient2(
    @Value("\${FLEX_FSS_PROXY_URL}") private val flexFssProxyUrl: String,
    private val flexFssProxyRestTemplate: RestTemplate,

) {

    val log = logger()

    fun ping() {
        val førPing = Instant.now()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val nyeHeaders = HttpHeaders()

        val forward: RequestEntity<Any> = RequestEntity(
            nyeHeaders,
            HttpMethod.GET,
            URI("$flexFssProxyUrl/internal/isAlive")
        )

        val responseEntity: ResponseEntity<Any> = flexFssProxyRestTemplate
            .exchange(
                forward
            )

        responseEntity.toString()

        Instant.now().let {
            val pingTid = (it.toEpochMilli() - førPing.toEpochMilli()).toInt()
            log.info("Latency mot flex-fss-proxy isALive med resttemplate med auth $pingTid ms")
        }
    }
}
