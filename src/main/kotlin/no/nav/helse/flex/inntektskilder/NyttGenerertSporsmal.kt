package no.nav.helse.flex.inntektskilder

import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.flex.bigquery.NyttGenerertSporsmalTable
import no.nav.helse.flex.bigquery.NyttSporsmal
import no.nav.helse.flex.client.ereg.EregClient
import no.nav.helse.flex.client.inntektskomponenten.InntektskomponentenClient
import no.nav.helse.flex.logger
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.sykepengesoknad.kafka.*
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

@Component
class NyttGenerertSporsmal(
    val nyttGenerertSporsmalTable: NyttGenerertSporsmalTable,
    val inntektskomponentenClient: InntektskomponentenClient,
    val eregClient: EregClient,
    private val registry: MeterRegistry

) {

    val log = logger()

    fun finnNyttSporsmal(soknad: SykepengesoknadDTO) {
        if (soknad.status != SoknadsstatusDTO.SENDT) {
            return
        }
        if (soknad.type != SoknadstypeDTO.ARBEIDSTAKERE) {
            return
        }

        val sykmeldingOrgnummer = soknad.arbeidsgiver!!.orgnummer!!
        val sykmeldingOrgnavn = soknad.arbeidsgiver!!.navn!!

        val førInntektskomp = Instant.now()
        val hentInntekter = inntektskomponentenClient
            .hentInntekter(
                soknad.fnr,
                fom = soknad.startSyketilfelle!!.yearMonth().minusMonths(3),
                tom = soknad.startSyketilfelle!!.yearMonth()
            )
        val etterInntektskomp = Instant.now()

        val inntekterOrgnummer = hentInntekter
            .arbeidsInntektMaaned
            .flatMap { it.arbeidsInntektInformasjon.inntektListe }
            .filter { it.inntektType == "LOENNSINNTEKT" }
            .filter { it.virksomhet.aktoerType == "ORGANISASJON" }
            .map { it.virksomhet.identifikator }
            .toSet()

        val førEreg = Instant.now()

        val inntekterOrgnavn = inntekterOrgnummer
            .filter { it != sykmeldingOrgnummer }
            .map { eregClient.hentBedrift(it) }
            .map { it.navn.navnelinje1 }
        val etterEreg = Instant.now()

        val latencyInntektskomp = (etterInntektskomp.toEpochMilli() - førInntektskomp.toEpochMilli()).toInt()
        log.info("Latency mot flex-fss-proxy / inntektskomp $latencyInntektskomp ms")

        registry.counter("spormsmal_generert").increment()
        nyttGenerertSporsmalTable.lagreNyttSporsmal(
            NyttSporsmal(
                sykepengesoknadId = soknad.id,
                nyttSporsmal = skapSporsmal(soknad.arbeidsgiver!!.navn!!, inntekterOrgnavn),
                sykmeldingOrgnummer = sykmeldingOrgnummer,
                sykmeldingOrgnavn = sykmeldingOrgnavn,
                orgnumreFraInntektskomponenten = inntekterOrgnummer.serialisertTilString(),
                haddeSykmeldingensOrgnummerHosInntektskomponenten = inntekterOrgnummer.contains(soknad.arbeidsgiver?.orgnummer),
                antallArbeidsforhold = inntekterOrgnummer.size,
                latencyEreg = (etterEreg.toEpochMilli() - førEreg.toEpochMilli()).toInt(),
                latencyInntektskomp = latencyInntektskomp
            )
        )
    }

    fun LocalDate.yearMonth() = YearMonth.from(this)
}
