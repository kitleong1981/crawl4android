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

    @Test
    fun usesNextProviderWhenResultsFailValidation() {
        val fallback = FallbackSearchProvider(
            providers = listOf(
                SearchProvider { _, _ -> listOf(SearchResult("Airbnb", "https://airbnb.example", "Vacation homes")) },
                SearchProvider { _, _ -> listOf(SearchResult("Android 17", "https://android.example", "Latest Android version")) }
            ),
            resultFilter = SearchResultRelevance::filterRelevantResults
        )

        val results = fallback.search("latest Android version", 5)

        assertEquals("Android 17", results.single().title)
    }

    @Test
    fun removesUnrelatedResultsFromAcceptedProvider() {
        val fallback = FallbackSearchProvider(
            providers = listOf(
                SearchProvider { _, _ -> listOf(
                    SearchResult("Android 17", "https://android.example", "Latest Android version"),
                    SearchResult("Airbnb", "https://airbnb.example", "Vacation homes")
                ) }
            ),
            resultFilter = SearchResultRelevance::filterRelevantResults
        )

        val results = fallback.search("latest Android version", 5)

        assertEquals(listOf("Android 17"), results.map(SearchResult::title))
    }

    @Test
    fun matchesSimpleEnglishPluralVariants() {
        val results = SearchResultRelevance.filterRelevantResults(
            "open-weight LLM models comparison",
            listOf(
                SearchResult("Best open-weight LLMs", "https://models.example", "Compare current language models"),
                SearchResult("Breaking news", "https://news.example", "Latest headlines and weather")
            )
        )

        assertEquals(listOf("Best open-weight LLMs"), results.map(SearchResult::title))
    }
}
