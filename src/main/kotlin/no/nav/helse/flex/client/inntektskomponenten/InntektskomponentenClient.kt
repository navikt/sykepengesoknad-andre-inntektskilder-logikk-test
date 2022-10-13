package no.nav.helse.flex.client.inntektskomponenten

import no.nav.helse.flex.logger
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.time.YearMonth

@Component
class InntektskomponentenClient(
    private val flexFssProxyRestTemplate: RestTemplate,
) {

    val log = logger()

    fun hentInntekter(fnr: String, fom: YearMonth, tom: YearMonth): HentInntekterResponse {

        val uriBuilder =
            UriComponentsBuilder.fromHttpUrl("http://sykepengesoknad-backend/api/v3/soknader/$fnr/kafkaformat")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val result = flexFssProxyRestTemplate
            .exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                HttpEntity<Any>(headers),
                HentInntekterResponse::class.java
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
