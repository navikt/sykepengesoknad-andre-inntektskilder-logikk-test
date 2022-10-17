package no.nav.helse.flex.inntektskilder

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class SkapSporsmalstekstTest {
    @Test
    internal fun testSporsmalstekst() {
        skapSporsmal("JPRO", emptyList()) `should be equal to` "Har du andre inntektskilder enn Jpro?"
        skapSporsmal("JPRO", listOf("NAV")) `should be equal to` "Har du andre inntektskilder enn Jpro og NAV?"
        skapSporsmal(
            "JPRO",
            listOf("NAV", "SKATTEETATEN")
        ) `should be equal to` "Har du andre inntektskilder enn Jpro, NAV og Skatteetaten?"
        skapSporsmal(
            "JPRO",
            listOf("NAV", "SKATTEETATEN", "KONGEHUSET")
        ) `should be equal to` "Har du andre inntektskilder enn Jpro, NAV, Skatteetaten og Kongehuset?"
    }
}
