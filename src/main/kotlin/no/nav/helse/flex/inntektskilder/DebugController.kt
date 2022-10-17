package no.nav.helse.flex.inntektskilder

import no.nav.helse.flex.client.inntektskomponenten.HentInntekterRequest
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.net.URI
import java.util.*
import javax.servlet.http.HttpServletResponse

@RestController
class DebugController(
    private val flexFssProxyRestTemplate: RestTemplate,
    @Value("\${FLEX_FSS_PROXY_URL}") private val flexFssProxyUrl: String,
    @Value("\${SHARED_SECRET}") private val sharedSecret: String,

) {

    @PostMapping(
        "/api/inntektskomponenten/api/v1/hentinntektliste/debug",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Unprotected
    fun hentInntektsliste(
        @RequestBody req: HentInntekterRequest,
        @RequestHeader authorization: String
    ): ResponseEntity<Any> {
        if (authorization != sharedSecret) {
            throw RuntimeException("Ingen tilgang")
        }
        val headers = HttpHeaders()
        headers["Nav-Consumer-Id"] = "srvflexfssproxy"
        headers["Nav-Call-Id"] = UUID.randomUUID().toString()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        val forward: RequestEntity<Any> = RequestEntity(
            req,
            headers,
            HttpMethod.POST,
            URI("$flexFssProxyUrl/api/inntektskomponenten/api/v1/hentinntektliste/debug")
        )

        val responseEntity: ResponseEntity<Any> = flexFssProxyRestTemplate.exchange(forward)

        val newHeaders: MultiValueMap<String, String> = LinkedMultiValueMap()
        responseEntity.headers.contentType?.let {
            newHeaders.set("Content-type", it.toString())
        }
        return responseEntity
    }

    @ExceptionHandler(HttpStatusCodeException::class)
    fun handleHttpStatusCodeException(response: HttpServletResponse, e: HttpStatusCodeException) {
        response.status = e.rawStatusCode
        if (e.responseHeaders != null) {
            val contentType = e.responseHeaders!!.contentType
            if (contentType != null) {
                response.contentType = contentType.toString()
            }
        }
        response.outputStream.write(e.responseBodyAsByteArray)
    }
}
