package io.github.kitleong1981.crawl4android

import org.junit.Assert.assertEquals
import org.junit.Test

class MojeekSearchProviderTest {
    private val provider = MojeekSearchProvider()

    @Test
    fun parsesStandardResults() {
        val html = """
            <html><body><ul class="results-standard">
              <li class="r1">
                <h2><a class="title" href="https://example.com/article">Example title</a></h2>
                <p class="s">Useful <strong>answer</strong> text.</p>
              </li>
            </ul></body></html>
        """.trimIndent()

        val results = provider.parseResults(html, "https://www.mojeek.com/search", 5)

        assertEquals(1, results.size)
        assertEquals("Example title", results.single().title)
        assertEquals("https://example.com/article", results.single().url)
        assertEquals("Useful answer text.", results.single().snippet)
    }
}
