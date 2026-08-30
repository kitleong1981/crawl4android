package io.github.kitleong1981.crawl4android

import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream

internal data class HttpPage(
    val finalUrl: String,
    val contentType: String,
    val body: String
)

internal class SafeHttpClient(
    private val config: CrawlConfig
) {
    fun get(url: String, acceptedTypes: Set<String>): HttpPage {
        var current = URI(url)
        repeat(MAX_REDIRECTS + 1) { redirectCount ->
            NetworkSafety.validatePublicWebUri(current)
            val connection = (current.toURL().openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                connectTimeout = config.connectTimeoutMillis
                readTimeout = config.readTimeoutMillis
                requestMethod = "GET"
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "text/html,text/plain;q=0.9,*/*;q=0.1")
                setRequestProperty("Accept-Language", "en-US,en;q=0.8")
            }

            try {
                val status = connection.responseCode
                if (status in REDIRECT_CODES) {
                    if (redirectCount == MAX_REDIRECTS) {
                        throw CrawlException("Too many redirects while loading $url")
                    }
                    val location = connection.getHeaderField("Location")
                        ?: throw CrawlException("Redirect response had no destination.")
                    current = current.resolve(location)
                    return@repeat
                }
                if (status !in 200..299) {
                    throw CrawlException("HTTP $status while loading ${current.host}")
                }

                val rawContentType = connection.contentType.orEmpty()
                val mediaType = rawContentType.substringBefore(';').trim().lowercase()
                if (mediaType !in acceptedTypes) {
                    throw CrawlException("Unsupported content type ${mediaType.ifBlank { "unknown" }}.")
                }

                val baseStream = connection.inputStream
                val stream = if (connection.contentEncoding.equals("gzip", ignoreCase = true)) {
                    GZIPInputStream(baseStream)
                } else {
                    baseStream
                }
                val bytes = stream.use { input ->
                    val output = ByteArrayOutputStream(minOf(config.maxPageBytes, 64 * 1024))
                    val buffer = ByteArray(16 * 1024)
                    var total = 0
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > config.maxPageBytes) {
                            throw CrawlException("Page exceeded the ${config.maxPageBytes}-byte safety limit.")
                        }
                        output.write(buffer, 0, count)
                    }
                    output.toByteArray()
                }
                val charset = parseCharset(rawContentType)
                return HttpPage(current.toString(), mediaType, bytes.toString(charset))
            } finally {
                connection.disconnect()
            }
        }
        throw CrawlException("Could not load $url")
    }

    private fun parseCharset(contentType: String): Charset {
        val name = contentType.split(';')
            .drop(1)
            .map { it.trim() }
            .firstOrNull { it.startsWith("charset=", ignoreCase = true) }
            ?.substringAfter('=')
            ?.trim(' ', '\'', '"')
        return name?.let { runCatching { Charset.forName(it) }.getOrNull() } ?: Charsets.UTF_8
    }

    companion object {
        private const val MAX_REDIRECTS = 5
        private val REDIRECT_CODES = setOf(301, 302, 303, 307, 308)
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 15; Mobile) AppleWebKit/537.36 Chrome/124 Mobile Safari/537.36 Crawl4Android/0.1"
    }
}

/** A user-displayable failure raised while searching, downloading, or extracting web content. */
class CrawlException(message: String, cause: Throwable? = null) : Exception(message, cause)
