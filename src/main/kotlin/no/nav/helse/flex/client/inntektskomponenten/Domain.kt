package no.nav.helse.flex.client.inntektskomponenten

data class HentInntekterResponse(
    val arbeidsInntektMaaned: List<ArbeidsInntektMaaned>,
    val ident: Ident
)

data class Virksomhet(
    val identifikator: String,
    val aktoerType: String
)

data class InntektListe(
    val inntektType: String,
    val virksomhet: Virksomhet,

)

data class Ident(
    val identifikator: String,
    val aktoerType: String
)

data class ArbeidsInntektMaaned(
    val aarMaaned: String,
    val arbeidsInntektInformasjon: ArbeidsInntektInformasjon
)

data class ArbeidsInntektInformasjon(
    val inntektListe: List<InntektListe>
)
