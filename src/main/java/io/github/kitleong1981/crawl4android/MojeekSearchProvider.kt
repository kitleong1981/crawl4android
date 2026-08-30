package io.github.kitleong1981.crawl4android

import org.jsoup.Jsoup
import java.net.URI
import java.net.URLEncoder

/** Search fallback that parses Mojeek's lightweight server-rendered result page. */
class MojeekSearchProvider(
    config: CrawlConfig = CrawlConfig()
) : SearchProvider {
    private val client = SafeHttpClient(config)

    override fun search(query: String, limit: Int): List<SearchResult> {
        val normalizedQuery = query.trim()
        require(normalizedQuery.isNotEmpty()) { "Search query cannot be empty." }
        val encoded = URLEncoder.encode(normalizedQuery, Charsets.UTF_8.name())
        val page = client.get(
            "https://www.mojeek.com/search?q=$encoded",
            acceptedTypes = setOf("text/html")
        )
        if (page.body.contains("captcha", ignoreCase = true)) {
            throw CrawlException("Mojeek requested browser verification.")
        }
        return parseResults(page.body, page.finalUrl, limit)
    }

    internal fun parseResults(html: String, baseUrl: String, limit: Int): List<SearchResult> {
        val document = Jsoup.parse(html, baseUrl)
        return document.select("ul.results-standard > li").mapNotNull { row ->
            val link = row.selectFirst("h2 a.title") ?: return@mapNotNull null
            val destination = link.attr("abs:href")
            val uri = runCatching { URI(destination) }.getOrNull() ?: return@mapNotNull null
            if (uri.scheme?.lowercase() !in setOf("http", "https")) return@mapNotNull null
            SearchResult(
                title = link.text().trim(),
                url = destination,
                snippet = row.selectFirst("p.s")?.text().orEmpty().trim()
            ).takeIf { it.title.isNotBlank() }
        }.distinctBy { it.url }.take(limit)
            .ifEmpty { throw CrawlException("Mojeek returned no searchable results.") }
    }
}
