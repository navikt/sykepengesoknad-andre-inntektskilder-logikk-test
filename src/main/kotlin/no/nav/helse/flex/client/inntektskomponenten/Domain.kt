package no.nav.helse.flex.client.inntektskomponenten

data class HentInntekterResponse(
    val arbeidsInntektMaaned: List<ArbeidsInntektMaaned>,
    val ident: Ident
)

data class Opplysningspliktig(
    val identifikator: String,
    val aktoerType: String
)

data class Virksomhet(
    val identifikator: String,
    val aktoerType: String
)

data class Inntektsmottaker(
    val identifikator: String,
    val aktoerType: String
)

data class InntektListe(
    val inntektType: String,
    val beloep: Int,
    val fordel: String,
    val inntektskilde: String,
    val inntektsperiodetype: String,
    val inntektsstatus: String,
    val leveringstidspunkt: String,
    val utbetaltIMaaned: String,
    val opplysningspliktig: Opplysningspliktig,
    val virksomhet: Virksomhet,
    val inntektsmottaker: Inntektsmottaker,
    val inngaarIGrunnlagForTrekk: Boolean,
    val utloeserArbeidsgiveravgift: Boolean,
    val informasjonsstatus: String,
    val beskrivelse: String
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
