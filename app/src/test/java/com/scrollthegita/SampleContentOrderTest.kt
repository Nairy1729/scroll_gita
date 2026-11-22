package com.scrollthegita

import com.scrollthegita.data.seed.SampleGitaContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SampleContentOrderTest {
    @Test
    fun seedHasEightToTenShlokasInSortOrder() {
        val shlokas = SampleGitaContent.shlokas
        assertTrue(shlokas.size in 8..10)
        assertEquals(shlokas.map { it.sortOrder }.sorted(), shlokas.map { it.sortOrder })
        assertEquals(shlokas.size, shlokas.map { it.id }.toSet().size)
        assertTrue(shlokas.all { it.sanskrit.contains("Verified Sanskrit text required") })
        assertEquals(18, SampleGitaContent.chapters.size)
    }
}
