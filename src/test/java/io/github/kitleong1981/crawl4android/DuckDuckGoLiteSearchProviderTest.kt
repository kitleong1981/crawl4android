package io.github.kitleong1981.crawl4android

import org.junit.Assert.assertEquals
import org.junit.Test

class DuckDuckGoLiteSearchProviderTest {
    private val provider = DuckDuckGoLiteSearchProvider()

    @Test
    fun parsesLiteResultsAndUnwrapsDestination() {
        val html = """
            <html><body><table>
              <tr><td>1.</td><td>
                <a class="result-link" href="//duckduckgo.com/l/?uddg=https%3A%2F%2Fexample.com%2Farticle&amp;rut=x">Example title</a>
              </td></tr>
              <tr><td></td><td class="result-snippet">Useful <b>answer</b> text.</td></tr>
            </table></body></html>
        """.trimIndent()

        val results = provider.parseResults(html, "https://lite.duckduckgo.com/lite/", 5)

        assertEquals(1, results.size)
        assertEquals("Example title", results.single().title)
        assertEquals("https://example.com/article", results.single().url)
        assertEquals("Useful answer text.", results.single().snippet)
    }
}
