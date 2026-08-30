package io.github.kitleong1981.crawl4android

import org.junit.Assert.assertEquals
import org.junit.Test

class YahooSearchProviderTest {
    private val provider = YahooSearchProvider()

    @Test
    fun parsesStandardResults() {
        val html = """
            <html><body>
              <section class="algo s-algo algo-sr">
                <h3><a class="s-title" href="https://example.com/model" aria-label="Model release">Example host Model release</a></h3>
                <p class="s-desc">A new <b>open-weight LLM</b> release.</p>
              </section>
            </body></html>
        """.trimIndent()

        val results = provider.parseResults(html, "https://search.yahoo.com/search", 5)

        assertEquals(1, results.size)
        assertEquals("Model release", results.single().title)
        assertEquals("https://example.com/model", results.single().url)
        assertEquals("A new open-weight LLM release.", results.single().snippet)
    }
}
