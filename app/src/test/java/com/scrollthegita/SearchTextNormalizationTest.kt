package com.scrollthegita

import com.scrollthegita.core.model.SearchText
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchTextNormalizationTest {

    @Test
    fun foldsLatinDiacriticsToAscii() {
        assertEquals("karma", SearchText.normalize("karmā"))
        assertEquals("karmani", SearchText.normalize("karmaṇi"))
        assertEquals("krsna", SearchText.normalize("kṛṣṇa"))
        assertEquals("dhrtarastra", SearchText.normalize("dhṛtarāṣṭra"))
        assertEquals("arjuna", SearchText.normalize("Arjuna"))
        assertEquals("sanjaya", SearchText.normalize("Sañjaya"))
    }

    @Test
    fun preservesDevanagariScript() {
        assertEquals("कर्मण्येवाधिकारस्ते", SearchText.normalize("कर्मण्येवाधिकारस्ते"))
        assertEquals("कृष्ण", SearchText.normalize("कृष्ण"))
        assertEquals("अर्जुन", SearchText.normalize("अर्जुन"))
    }

    @Test
    fun collapsesWhitespaceAndTrims() {
        assertEquals("karma yoga", SearchText.normalize("   karma    yoga   "))
        assertEquals("action", SearchText.normalize("\n\taction\n"))
    }

    @Test
    fun ftsQueryGeneratesSafePrefixTokens() {
        assertEquals("karma*", SearchText.ftsQuery("karmā"))
        assertEquals("karma* yoga*", SearchText.ftsQuery("karmā yoga"))
        assertEquals("krsna* arjuna*", SearchText.ftsQuery("kṛṣṇa & arjuna!"))
        assertEquals("कर्म*", SearchText.ftsQuery("कर्म"))
        assertEquals("", SearchText.ftsQuery("   "))
        assertEquals("", SearchText.ftsQuery("!@#$%^&*()"))
    }

    @Test
    fun likePatternEscapesSqlWildcards() {
        assertEquals("%karma%", SearchText.likePattern("karma"))
        assertEquals("%100\\%%", SearchText.likePattern("100%"))
        assertEquals("%under\\_score%", SearchText.likePattern("under_score"))
        assertEquals("%path\\\\to%", SearchText.likePattern("path\\to"))
    }
}
