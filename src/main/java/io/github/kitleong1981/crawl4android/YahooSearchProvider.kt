package io.github.kitleong1981.crawl4android

import org.jsoup.Jsoup
import java.net.URI
import java.net.URLEncoder

/** Search fallback that parses Yahoo's server-rendered result page. */
class YahooSearchProvider(
    config: CrawlConfig = CrawlConfig()
) : SearchProvider {
    private val client = SafeHttpClient(config)

    override fun search(query: String, limit: Int): List<SearchResult> {
        val normalizedQuery = query.trim()
        require(normalizedQuery.isNotEmpty()) { "Search query cannot be empty." }
        val encoded = URLEncoder.encode(normalizedQuery, Charsets.UTF_8.name())
        val page = client.get(
            "https://search.yahoo.com/search?p=$encoded",
            acceptedTypes = setOf("text/html")
        )
        return parseResults(page.body, page.finalUrl, limit)
    }

    internal fun parseResults(html: String, baseUrl: String, limit: Int): List<SearchResult> {
        val document = Jsoup.parse(html, baseUrl)
        return document.select("section.algo-sr").mapNotNull { row ->
            val link = row.selectFirst("h3 a.s-title") ?: return@mapNotNull null
            val destination = link.attr("abs:href")
            val uri = runCatching { URI(destination) }.getOrNull() ?: return@mapNotNull null
            if (uri.scheme?.lowercase() !in setOf("http", "https")) return@mapNotNull null
            SearchResult(
                title = link.attr("aria-label").ifBlank { link.ownText() }.trim(),
                url = destination,
                snippet = row.selectFirst("p.s-desc")?.text().orEmpty().trim()
            ).takeIf { it.title.isNotBlank() }
        }.distinctBy { it.url }.take(limit)
            .ifEmpty { throw CrawlException("Yahoo returned no searchable results.") }
    }
}
