package io.github.kitleong1981.crawl4android

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

internal data class ContentChunk(
    val source: SearchResult,
    val text: String
)

internal class PageContentExtractor(
    private val targetChunkChars: Int
) {
    fun extract(page: HttpPage, searchResult: SearchResult): List<ContentChunk> {
        val document = Jsoup.parse(page.body, page.finalUrl)
        document.select(REMOVE_SELECTORS).remove()
        document.select("[hidden], [aria-hidden=true]").remove()
        document.select("[class], [id]").forEach { element ->
            val marker = "${element.className()} ${element.id()}".lowercase()
            if (NOISE_MARKERS.any(marker::contains)) {
                element.remove()
            }
        }

        val root = chooseContentRoot(document.body(), document.select("article, main, [role=main]"))
        val blocks = root.select("h1, h2, h3, p, li, pre, blockquote")
            .map { normalizeWhitespace(it.text()) }
            .filter { text -> text.length >= MIN_BLOCK_CHARS || text.count(Char::isLetterOrDigit) >= 18 }
            .distinct()
            .ifEmpty {
                splitFallback(normalizeWhitespace(root.text()))
            }
        return packBlocks(blocks).map { ContentChunk(searchResult, it) }
    }

    private fun chooseContentRoot(body: Element, candidates: List<Element>): Element {
        return candidates.maxByOrNull { candidate ->
            val textLength = candidate.text().length
            val linkLength = candidate.select("a").sumOf { it.text().length }
            textLength - (linkLength * 2)
        }?.takeIf { it.text().length >= 300 } ?: body
    }

    private fun packBlocks(blocks: List<String>): List<String> {
        val chunks = mutableListOf<String>()
        val current = StringBuilder()
        blocks.forEach { block ->
            if (block.length > targetChunkChars * 2) {
                flush(current, chunks)
                chunks += splitLongText(block)
            } else if (current.isNotEmpty() && current.length + block.length + 2 > targetChunkChars) {
                flush(current, chunks)
                current.append(block)
            } else {
                if (current.isNotEmpty()) current.append("\n\n")
                current.append(block)
            }
        }
        flush(current, chunks)
        return chunks.filter { it.length >= MIN_CHUNK_CHARS }.take(MAX_CHUNKS_PER_PAGE)
    }

    private fun splitFallback(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        return splitLongText(text)
    }

    private fun splitLongText(text: String): List<String> {
        val sentences = text.split(Regex("(?<=[.!?。！？])\\s+"))
        if (sentences.size == 1) {
            return text.chunked(targetChunkChars)
        }
        val chunks = mutableListOf<String>()
        val current = StringBuilder()
        sentences.forEach { sentence ->
            if (current.isNotEmpty() && current.length + sentence.length + 1 > targetChunkChars) {
                flush(current, chunks)
            }
            if (current.isNotEmpty()) current.append(' ')
            current.append(sentence)
        }
        flush(current, chunks)
        return chunks
    }

    private fun flush(builder: StringBuilder, output: MutableList<String>) {
        val value = builder.toString().trim()
        if (value.isNotEmpty()) output += value
        builder.clear()
    }

    private fun normalizeWhitespace(text: String): String {
        return text.replace(Regex("[\\t\\x0B\\f\\r ]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    companion object {
        private const val MIN_BLOCK_CHARS = 35
        private const val MIN_CHUNK_CHARS = 80
        private const val MAX_CHUNKS_PER_PAGE = 40
        private const val REMOVE_SELECTORS =
            "script, style, noscript, svg, canvas, iframe, nav, footer, header, aside, form, button, input, select, textarea"
        private val NOISE_MARKERS = listOf(
            "advert", "cookie", "consent", "newsletter", "subscribe", "sidebar", "breadcrumb",
            "social-share", "related-post", "recommended", "popup", "modal", "navigation", "site-header",
            "site-footer"
        )
    }
}
