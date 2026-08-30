package io.github.kitleong1981.crawl4android

import org.junit.Assert.assertEquals
import org.junit.Test

class Bm25RankerTest {
    @Test
    fun ranksQueryRelevantChunkFirst() {
        val source = SearchResult("Test", "https://example.com", "")
        val chunks = listOf(
            ContentChunk(source, "A recipe explains how to bake bread with flour and yeast."),
            ContentChunk(source, "LiteRT GPU acceleration improves local Android model inference speed.")
        )

        val ranked = Bm25Ranker.rank("Android LiteRT GPU inference", chunks)

        assertEquals(chunks[1], ranked.first().chunk)
    }

    @Test
    fun ranksChineseQueryWithCjkBigrams() {
        val source = SearchResult("Test", "https://example.com", "")
        val chunks = listOf(
            ContentChunk(source, "這篇文章介紹如何烤麵包和準備晚餐。"),
            ContentChunk(source, "Android 手機上的本機模型可以使用 GPU 加速推理速度。")
        )

        val ranked = Bm25Ranker.rank("手機本機模型推理", chunks)

        assertEquals(chunks[1], ranked.first().chunk)
    }
}
