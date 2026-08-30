package io.github.kitleong1981.crawl4android

import org.jsoup.Jsoup
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Search provider that parses DuckDuckGo Lite HTML.
 *
 * DuckDuckGo does not guarantee this HTML interface, so hosts should handle [CrawlException].
 */
class DuckDuckGoLiteSearchProvider(
    config: CrawlConfig = CrawlConfig()
) : SearchProvider {
    private val client = SafeHttpClient(config)

    override fun search(query: String, limit: Int): List<SearchResult> {
        val normalizedQuery = query.trim()
        require(normalizedQuery.isNotEmpty()) { "Search query cannot be empty." }
        val encoded = URLEncoder.encode(normalizedQuery, Charsets.UTF_8.name())
        val page = client.get(
            "https://lite.duckduckgo.com/lite/?q=$encoded",
            acceptedTypes = setOf("text/html")
        )
        if (
            page.body.contains("anomaly-modal", ignoreCase = true) ||
            page.body.contains("bots use DuckDuckGo too", ignoreCase = true)
        ) {
            throw CrawlException("DuckDuckGo requested a browser verification. Try again later.")
        }

        return parseResults(page.body, page.finalUrl, limit)
    }

    internal fun parseResults(html: String, baseUrl: String, limit: Int): List<SearchResult> {
        val document = Jsoup.parse(html, baseUrl)
        val links = document.select("a.result-link, a.result__a")
        val results = links.mapNotNull { link ->
            val destination = unwrapDuckDuckGoUrl(link.attr("abs:href")) ?: return@mapNotNull null
            val row = link.closest("tr")
            val snippetRow = row?.nextElementSibling()
            val snippet = snippetRow?.selectFirst("td.result-snippet, .result__snippet")?.text()
                ?: link.parent()?.parent()?.selectFirst(".result-snippet, .result__snippet")?.text()
                ?: ""
            SearchResult(
                title = link.text().trim(),
                url = destination,
                snippet = snippet.trim()
            ).takeIf { it.title.isNotBlank() }
        }
        return results.distinctBy { it.url }.take(limit)
            .ifEmpty { throw CrawlException("DuckDuckGo returned no searchable results.") }
    }

    internal fun unwrapDuckDuckGoUrl(rawUrl: String): String? {
        val absolute = when {
            rawUrl.startsWith("//") -> "https:$rawUrl"
            rawUrl.startsWith("/") -> "https://duckduckgo.com$rawUrl"
            else -> rawUrl
        }
        val uri = runCatching { URI(absolute) }.getOrNull() ?: return null
        val wrapped = uri.rawQuery.orEmpty().split('&')
            .mapNotNull { part ->
                val key = part.substringBefore('=', "")
                val value = part.substringAfter('=', "")
                if (key == "uddg") URLDecoder.decode(value, Charsets.UTF_8.name()) else null
            }
            .firstOrNull()
        val destination = wrapped ?: absolute
        val destinationUri = runCatching { URI(destination) }.getOrNull() ?: return null
        return destination.takeIf { destinationUri.scheme?.lowercase() in setOf("http", "https") }
    }
}
