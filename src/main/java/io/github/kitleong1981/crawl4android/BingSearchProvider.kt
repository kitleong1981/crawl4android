package io.github.kitleong1981.crawl4android

import org.jsoup.Jsoup
import java.net.URI
import java.net.URLEncoder

/** Search fallback that parses Bing's server-rendered web result list. */
class BingSearchProvider(
    config: CrawlConfig = CrawlConfig()
) : SearchProvider {
    private val client = SafeHttpClient(config)

    override fun search(query: String, limit: Int): List<SearchResult> {
        val normalizedQuery = query.trim()
        require(normalizedQuery.isNotEmpty()) { "Search query cannot be empty." }
        val encoded = URLEncoder.encode(normalizedQuery, Charsets.UTF_8.name())
        val page = client.get(
            "https://www.bing.com/search?q=$encoded",
            acceptedTypes = setOf("text/html")
        )
        return parseResults(page.body, page.finalUrl, limit)
    }

    internal fun parseResults(html: String, baseUrl: String, limit: Int): List<SearchResult> {
        val document = Jsoup.parse(html, baseUrl)
        return document.select("li.b_algo").mapNotNull { row ->
            val link = row.selectFirst(".b_algoheader > a, h2 > a") ?: return@mapNotNull null
            val destination = link.attr("abs:href")
            val uri = runCatching { URI(destination) }.getOrNull() ?: return@mapNotNull null
            if (uri.scheme?.lowercase() !in setOf("http", "https")) return@mapNotNull null
            SearchResult(
                title = link.text().trim(),
                url = destination,
                snippet = row.selectFirst(".b_caption p")?.text().orEmpty().trim()
            ).takeIf { it.title.isNotBlank() }
        }.distinctBy { it.url }.take(limit)
            .ifEmpty { throw CrawlException("Bing returned no searchable results.") }
    }
}
