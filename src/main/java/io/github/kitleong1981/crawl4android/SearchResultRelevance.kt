package io.github.kitleong1981.crawl4android

internal object SearchResultRelevance {
    private val ignoredTerms = setOf(
        "a", "an", "and", "are", "about", "compare", "comparison", "find", "for", "from",
        "feature", "features", "in", "is", "latest", "list", "month", "months", "new", "news",
        "of", "on", "online", "or", "past", "please", "recent", "release", "released", "releases",
        "search", "summarize", "the", "their", "to", "today", "two", "what", "which", "with"
    )

    fun hasRelevantResults(query: String, results: List<SearchResult>): Boolean {
        return filterRelevantResults(query, results).isNotEmpty()
    }

    fun filterRelevantResults(query: String, results: List<SearchResult>): List<SearchResult> {
        val queryTerms = meaningfulTerms(query)
        if (queryTerms.isEmpty()) return results
        val requiredMatches = if (queryTerms.size == 1) 1 else 2
        return results.filter { result ->
            val resultTerms = Bm25Ranker.tokenize("${result.title} ${result.snippet}")
                .map(::normalizeTerm)
                .toSet()
            queryTerms.count { it in resultTerms } >= requiredMatches
        }
    }

    private fun meaningfulTerms(text: String): Set<String> {
        return Bm25Ranker.tokenize(text)
            .filterNot { it in ignoredTerms || it.all(Char::isDigit) }
            .map(::normalizeTerm)
            .toSet()
    }

    private fun normalizeTerm(term: String): String {
        return if (term.length > 3 && term.all(Char::isLetter) && term.endsWith('s')) {
            term.dropLast(1)
        } else {
            term
        }
    }
}
