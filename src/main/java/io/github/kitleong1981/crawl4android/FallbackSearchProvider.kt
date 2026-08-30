package io.github.kitleong1981.crawl4android

/** Tries search providers in order without attempting to bypass provider challenges. */
class FallbackSearchProvider(
    private val providers: List<SearchProvider>,
    private val resultFilter: (String, List<SearchResult>) -> List<SearchResult> = { _, results -> results }
) : SearchProvider {
    init {
        require(providers.isNotEmpty()) { "At least one search provider is required." }
    }

    override fun search(query: String, limit: Int): List<SearchResult> {
        val failures = mutableListOf<String>()
        providers.forEach { provider ->
            try {
                val results = provider.search(query, limit)
                val relevantResults = resultFilter(query, results)
                if (relevantResults.isNotEmpty()) return relevantResults.take(limit)
                failures += "${provider.javaClass.simpleName} returned unrelated results."
            } catch (error: Exception) {
                failures += error.message ?: provider.javaClass.simpleName
            }
        }
        throw CrawlException(
            "Web search providers are temporarily unavailable. ${failures.joinToString(" ")}".trim()
        )
    }
}
