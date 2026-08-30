package io.github.kitleong1981.crawl4android

/** A search-engine result before its destination page is downloaded. */
data class SearchResult(
    val title: String,
    val url: String,
    val snippet: String
)

/** Selected excerpts from one source, ready for citation by the host application. */
data class CrawledSource(
    val title: String,
    val url: String,
    val excerpts: List<String>
)

/** The bounded model context and structured source metadata produced by a research run. */
data class CrawlReport(
    val query: String,
    val sources: List<CrawledSource>,
    val context: String,
    val searchResultCount: Int,
    val crawledPageCount: Int,
    val warnings: List<String>
)

/** Resource and output limits applied to search and page downloads. */
data class CrawlConfig(
    val maxSearchResults: Int = 5,
    val maxPagesToCrawl: Int = 3,
    val maxPageBytes: Int = 1_500_000,
    val maxContextChars: Int = 8_000,
    val chunkTargetChars: Int = 900,
    val connectTimeoutMillis: Int = 8_000,
    val readTimeoutMillis: Int = 10_000
) {
    init {
        require(maxSearchResults in 1..20)
        require(maxPagesToCrawl in 0..maxSearchResults)
        require(maxPageBytes in 64_000..5_000_000)
        require(maxContextChars in 1_000..40_000)
        require(chunkTargetChars in 200..3_000)
    }
}

/** Coarse progress events suitable for mapping to host-application UI. */
sealed class CrawlProgress {
    data object Searching : CrawlProgress()
    data class Fetching(val current: Int, val total: Int, val title: String) : CrawlProgress()
    data object Ranking : CrawlProgress()
}

/** Pluggable search backend used to find candidate pages. */
fun interface SearchProvider {
    fun search(query: String, limit: Int): List<SearchResult>
}
