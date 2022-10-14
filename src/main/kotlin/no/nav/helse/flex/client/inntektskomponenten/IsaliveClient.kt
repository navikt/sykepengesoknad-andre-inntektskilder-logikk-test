package no.nav.helse.flex.client.inntektskomponenten

import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.flex.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class IsaliveClient(
    private val plainRestTemplate: RestTemplate,
    @Value("\${FLEX_FSS_PROXY_URL}") private val flexFssProxyUrl: String,

) {

    val log = logger()

    fun ping(): JsonNode {

        val uriBuilder =
            UriComponentsBuilder.fromHttpUrl("$flexFssProxyUrl/internal/isAlive")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val result = plainRestTemplate
            .exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                HttpEntity<Any>(headers),
                JsonNode::class.java
            )

        if (result.statusCode != OK) {
            val message = "Kall mot syfosoknad feiler med HTTP-" + result.statusCode
            log.error(message)
            throw RuntimeException(message)
        }

        result.body?.let { return it }

        val message = "Kall mot syfosoknad returnerer ikke data"
        log.error(message)
        throw RuntimeException(message)
    }
}
