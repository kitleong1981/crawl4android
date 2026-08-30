package io.github.kitleong1981.crawl4android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageContentExtractorTest {
    @Test
    fun removesNavigationAndKeepsArticleContent() {
        val html = """
            <html><body>
              <nav>Menu pricing login account</nav>
              <main>
                <h1>Android local inference</h1>
                <p>LiteRT runs language models directly on supported Android devices using accelerated backends.</p>
                <p>Keeping source excerpts short leaves more context space for the model answer.</p>
              </main>
              <footer>Copyright and newsletter subscription</footer>
            </body></html>
        """.trimIndent()
        val source = SearchResult("Test", "https://example.com", "")

        val chunks = PageContentExtractor(300).extract(
            HttpPage(source.url, "text/html", html),
            source
        )
        val text = chunks.joinToString("\n") { it.text }

        assertTrue(text.contains("LiteRT runs language models"))
        assertFalse(text.contains("Menu pricing"))
        assertFalse(text.contains("newsletter subscription"))
    }
}
