package io.github.kitleong1981.crawl4android

import org.junit.Assert.assertTrue
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
}
