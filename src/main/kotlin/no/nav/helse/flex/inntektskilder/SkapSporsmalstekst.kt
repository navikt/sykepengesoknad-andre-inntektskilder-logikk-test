package no.nav.helse.flex.inntektskilder

fun skapSporsmal(sykmeldingOrgnavn: String, andreOrgnavn: List<String>): String {
    val listen = mutableListOf(sykmeldingOrgnavn).also { it.addAll(andreOrgnavn) }

    fun virksomheterTekst(): String {
        if (listen.size < 3) {
            return listen.joinToString(" og ")
        }
        return "${listen.subList(0, listen.size - 1).joinToString(", ")} og ${listen.last()}"
    }

    return "Har du andre inntektskilder enn ${virksomheterTekst()}?"
}
