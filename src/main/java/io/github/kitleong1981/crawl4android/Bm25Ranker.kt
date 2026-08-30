package io.github.kitleong1981.crawl4android

import java.util.Locale
import kotlin.math.ln

internal data class RankedChunk(
    val chunk: ContentChunk,
    val score: Double
)

internal object Bm25Ranker {
    private const val K1 = 1.5
    private const val B = 0.75
    private val SEGMENT_REGEX = Regex("[\\p{L}\\p{N}]+")

    fun rank(query: String, chunks: List<ContentChunk>): List<RankedChunk> {
        if (chunks.isEmpty()) return emptyList()
        val queryTerms = tokenize(query).distinct()
        if (queryTerms.isEmpty()) {
            return chunks.map { RankedChunk(it, 0.0) }
        }
        val documents = chunks.map { tokenize(it.text) }
        val averageLength = documents.map { it.size }.average().coerceAtLeast(1.0)
        val documentFrequency = queryTerms.associateWith { term ->
            documents.count { term in it.toSet() }
        }

        return chunks.indices.map { index ->
            val terms = documents[index]
            val frequencies = terms.groupingBy { it }.eachCount()
            val score = queryTerms.sumOf { term ->
                val frequency = frequencies[term]?.toDouble() ?: 0.0
                if (frequency == 0.0) return@sumOf 0.0
                val containingDocuments = documentFrequency.getValue(term).toDouble()
                val idf = ln(1.0 + (chunks.size - containingDocuments + 0.5) / (containingDocuments + 0.5))
                val denominator = frequency + K1 * (1.0 - B + B * terms.size / averageLength)
                idf * (frequency * (K1 + 1.0)) / denominator
            }
            RankedChunk(chunks[index], score)
        }.sortedByDescending { it.score }
    }

    internal fun tokenize(text: String): List<String> {
        return SEGMENT_REGEX.findAll(text.lowercase(Locale.ROOT)).flatMap { match ->
            splitSegment(match.value).asSequence()
        }.toList()
    }

    private fun splitSegment(segment: String): List<String> {
        val output = mutableListOf<String>()
        val nonHan = StringBuilder()
        val han = StringBuilder()

        fun flushNonHan() {
            nonHan.toString().takeIf { it.length >= 2 }?.let(output::add)
            nonHan.clear()
        }
        fun flushHan() {
            val value = han.toString()
            when (value.length) {
                0 -> Unit
                1 -> output += value
                else -> value.windowed(2, 1).forEach(output::add)
            }
            han.clear()
        }

        segment.forEach { character ->
            if (Character.UnicodeScript.of(character.code) == Character.UnicodeScript.HAN) {
                flushNonHan()
                han.append(character)
            } else {
                flushHan()
                nonHan.append(character)
            }
        }
        flushHan()
        flushNonHan()
        return output
    }
}
