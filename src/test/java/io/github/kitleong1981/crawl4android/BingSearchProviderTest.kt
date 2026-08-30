package io.github.kitleong1981.crawl4android

import org.junit.Assert.assertEquals
import org.junit.Test

class BingSearchProviderTest {
    private val provider = BingSearchProvider()

    @Test
    fun parsesStandardResults() {
        val html = """
            <html><body><ol id="b_results">
              <li class="b_algo">
                <div class="b_algoheader"><a href="https://example.com/article"><h2>Example title</h2></a></div>
                <div class="b_caption"><p>Useful <strong>answer</strong> text.</p></div>
              </li>
            </ol></body></html>
        """.trimIndent()

        val results = provider.parseResults(html, "https://www.bing.com/search", 5)

        assertEquals(1, results.size)
        assertEquals("Example title", results.single().title)
        assertEquals("https://example.com/article", results.single().url)
        assertEquals("Useful answer text.", results.single().snippet)
    }
}
