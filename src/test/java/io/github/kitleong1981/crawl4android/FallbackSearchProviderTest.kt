package io.github.kitleong1981.crawl4android

import org.junit.Assert.assertEquals
import org.junit.Test

class FallbackSearchProviderTest {
    @Test
    fun usesNextProviderAfterFailure() {
        val fallback = FallbackSearchProvider(
            listOf(
                SearchProvider { _, _ -> throw CrawlException("verification required") },
                SearchProvider { _, _ -> listOf(SearchResult("Fallback", "https://example.com", "OK")) }
            )
        )

        val results = fallback.search("query", 5)

        assertEquals("Fallback", results.single().title)
    }
}
