package io.github.kitleong1981.crawl4android

import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class Crawl4AndroidTest {
    @Test
    fun buildsBoundedCitedContextFromSearchSnippets() {
        val provider = SearchProvider { _, _ ->
            listOf(
                SearchResult("GPU guide", "https://example.com/gpu", "Android GPU acceleration can improve local inference."),
                SearchResult("Cooking", "https://example.com/food", "A recipe for soup and bread.")
            )
        }
        val crawler = Crawl4Android(
            config = CrawlConfig(maxPagesToCrawl = 0, maxContextChars = 2_000),
            searchProvider = provider
        )

        val report = crawler.research("Android GPU inference")

        assertTrue(report.context.length <= 2_000)
        assertTrue(report.context.contains("[Source 1]"))
        assertTrue(report.context.contains("https://example.com/gpu"))
        assertTrue(report.context.contains("untrusted reference data"))
    }

    @Test
    fun mergesMultipleQueriesWithoutDuplicatingSources() {
        val provider = SearchProvider { query, _ ->
            when {
                query.contains("Hugging Face") -> listOf(
                    SearchResult("Model A", "https://example.com/a", "Open weight LLM release August 2026"),
                    SearchResult("Shared", "https://example.com/shared", "Open model release")
                )
                else -> listOf(
                    SearchResult("Model B", "https://example.com/b", "New LLM model July 2026"),
                    SearchResult("Shared", "https://example.com/shared", "Open model release")
                )
            }
        }
        val crawler = Crawl4Android(
            config = CrawlConfig(maxPagesToCrawl = 0, maxContextChars = 2_000),
            searchProvider = provider
        )

        val report = crawler.research(
            "recent open weight LLM releases",
            listOf("Hugging Face model releases August 2026", "new LLM models July 2026")
        )

        assertEquals(3, report.searchResultCount)
        assertEquals(3, report.sources.size)
    }
}
