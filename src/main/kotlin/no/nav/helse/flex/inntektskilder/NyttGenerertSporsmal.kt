package no.nav.helse.flex.inntektskilder

import no.nav.helse.flex.bigquery.NyttGenerertSporsmalTable
import no.nav.helse.flex.bigquery.NyttSporsmal
import no.nav.helse.flex.client.ereg.EregClient
import no.nav.helse.flex.client.inntektskomponenten.InntektskomponentenClient
import no.nav.helse.flex.sykepengesoknad.kafka.*
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.YearMonth

@Component
class NyttGenerertSporsmal(
    val nyttGenerertSporsmalTable: NyttGenerertSporsmalTable,
    val inntektskomponentenClient: InntektskomponentenClient,
    val eregClient: EregClient
) {

    fun finnNyttSporsmal(soknad: SykepengesoknadDTO) {
        if (soknad.status != SoknadsstatusDTO.SENDT) {
            return
        }
        if (soknad.type != SoknadstypeDTO.ARBEIDSTAKERE) {
            return
        }

        val sykmeldingOrgnummer = soknad.arbeidsgiver!!.orgnummer!!
        val sykmeldingOrgnavn = soknad.arbeidsgiver!!.navn!!

        val inntekterOrgnummer = inntektskomponentenClient
            .hentInntekter(
                soknad.fnr,
                fom = soknad.startSyketilfelle!!.yearMonth().minusMonths(3),
                tom = soknad.startSyketilfelle!!.yearMonth()
            )
            .arbeidsInntektMaaned
            .flatMap { it.arbeidsInntektInformasjon.inntektListe }
            .filter { it.inntektType == "LOENNSINNTEKT" }
            .filter { it.virksomhet.aktoerType == "ORGANISASJON" }
            .map { it.virksomhet.identifikator }
            .toSet()

        val inntekterOrgnavn = inntekterOrgnummer
            .filter { it != sykmeldingOrgnummer }
            .map { eregClient.hentBedrift(it) }
            .map { it.navn.navnelinje1 }

        nyttGenerertSporsmalTable.lagreNyttSporsmal(
            NyttSporsmal(
                sykepengesoknadId = soknad.id,
                nyttSporsmal = skapSporsmal(soknad.arbeidsgiver!!.navn!!, inntekterOrgnavn),
                sykmeldingOrgnummer = sykmeldingOrgnummer,
                sykmeldingOrgnavn = sykmeldingOrgnavn,
                orgnumreFraInntektskomponenten = soknad.id,
                haddeSykmeldingensOrgnummerHosInntektskomponenten = inntekterOrgnummer.contains(soknad.arbeidsgiver?.orgnummer),
                antallArbeidsforhold = inntekterOrgnummer.size,
            )
        )
    }

    fun LocalDate.yearMonth() = YearMonth.from(this)
}
