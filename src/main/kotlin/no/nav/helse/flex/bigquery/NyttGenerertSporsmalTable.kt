package no.nav.helse.flex.bigquery

import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.TableId
import no.nav.helse.flex.logger
import org.springframework.stereotype.Component
import kotlin.collections.HashMap

@Component
class NyttGenerertSporsmalTable(val bq: BigQuery) {

    val log = logger()

    fun lagreNyttSporsmal(ks: NyttSporsmal) {

        val insertAll = bq.insertAll(
            InsertAllRequest.newBuilder(TableId.of(dataset, tableName))
                .also { builder ->
                    builder.addRow(ks.tilMap())
                }
                .build()
        )

        if (insertAll.hasErrors()) {
            insertAll.insertErrors.forEach { (t, u) -> log.error("$t - $u") }
            throw RuntimeException("Bigquery insert har errors")
        }
    }
}

private fun NyttSporsmal.tilMap(): Map<String, Any> {
    val data: MutableMap<String, Any> = HashMap()
    data["sykepengesoknadId"] = sykepengesoknadId
    data["nyttSporsmal"] = nyttSporsmal
    data["sykmeldingOrgnummer"] = sykmeldingOrgnummer
    data["sykmeldingOrgnavn"] = sykmeldingOrgnavn
    data["orgnumreFraInntektskomponenten"] = orgnumreFraInntektskomponenten
    data["haddeSykmeldingensOrgnummerHosInntektskomponenten"] = haddeSykmeldingensOrgnummerHosInntektskomponenten
    data["antallArbeidsforhold"] = antallArbeidsforhold

    return data.toMap()
}

data class NyttSporsmal(
    val sykepengesoknadId: String,
    val nyttSporsmal: String,
    val sykmeldingOrgnummer: String,
    val sykmeldingOrgnavn: String,
    val orgnumreFraInntektskomponenten: String,
    val haddeSykmeldingensOrgnummerHosInntektskomponenten: Boolean,
    val antallArbeidsforhold: Int,
)
