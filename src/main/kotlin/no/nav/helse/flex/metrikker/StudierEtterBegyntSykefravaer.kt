package no.nav.helse.flex.metrikker

import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.flex.logger
import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class StudierEtterBegyntSykefravaer(
    val registry: MeterRegistry
) {

    val log = logger()

    fun finnBegyntStudierFoerSyk(soknad: SykepengesoknadDTO) {
        if (soknad.status != SoknadsstatusDTO.SENDT) {
            return
        }
        soknad.sporsmal!!
            .filter { it.tag == "UTDANNING" }
            .filter { it.svar?.firstOrNull()?.verdi == "JA" }
            .flatMap { it.undersporsmal!! }
            .firstOrNull { it.tag == "UTDANNING_START" }
            ?.let { s ->
                val svarVerdi = s.svar?.firstOrNull { it.verdi != null }?.verdi
                if (svarVerdi == null) {
                    log.warn("Fant besvart UTDANNING-spørsmål uten dato")
                    return
                }
                val startSyketilfelle = soknad.startSyketilfelle!!
                val startUtdanning = LocalDate.parse(svarVerdi)
                val etterSykefravaer = startSyketilfelle.isBefore(startUtdanning)
                registry.counter("utdanning_start", "etter_sykefravaer", etterSykefravaer.toString()).increment()
            }
    }
}
