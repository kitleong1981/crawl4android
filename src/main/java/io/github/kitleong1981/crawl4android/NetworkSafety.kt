package io.github.kitleong1981.crawl4android

import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.URI

internal object NetworkSafety {
    fun validatePublicWebUri(uri: URI) {
        if (uri.scheme?.lowercase() !in setOf("http", "https")) {
            throw CrawlException("Only HTTP and HTTPS pages are supported.")
        }
        if (uri.userInfo != null) {
            throw CrawlException("URLs containing credentials are not supported.")
        }
        val host = uri.host?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw CrawlException("URL has no valid host.")
        if (host.equals("localhost", true) || host.endsWith(".localhost", true)) {
            throw CrawlException("Local network URLs are blocked.")
        }
        val addresses = runCatching { InetAddress.getAllByName(host).toList() }
            .getOrElse { throw CrawlException("Could not resolve $host", it) }
        if (addresses.isEmpty() || addresses.any(::isBlockedAddress)) {
            throw CrawlException("Private or local network URLs are blocked.")
        }
    }

    internal fun isBlockedAddress(address: InetAddress): Boolean {
        if (
            address.isAnyLocalAddress || address.isLoopbackAddress || address.isLinkLocalAddress ||
            address.isSiteLocalAddress || address.isMulticastAddress
        ) {
            return true
        }
        val bytes = address.address
        return when (address) {
            is Inet4Address -> {
                val first = bytes[0].toInt() and 0xff
                val second = bytes[1].toInt() and 0xff
                first == 0 || first == 127 ||
                    (first == 100 && second in 64..127) ||
                    (first == 169 && second == 254)
            }
            is Inet6Address -> (bytes[0].toInt() and 0xfe) == 0xfc
            else -> true
        }
    }
}
