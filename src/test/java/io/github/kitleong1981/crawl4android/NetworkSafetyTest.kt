package io.github.kitleong1981.crawl4android

import java.net.InetAddress
import java.net.URI
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkSafetyTest {
    @Test
    fun blocksLocalAndPrivateDestinations() {
        listOf(
            "http://localhost/",
            "http://127.0.0.1/",
            "http://10.0.0.1/",
            "http://100.64.0.1/",
            "http://169.254.1.1/",
            "http://[fc00::1]/"
        ).forEach { url ->
            assertThrows(url, CrawlException::class.java) {
                NetworkSafety.validatePublicWebUri(URI(url))
            }
        }
    }

    @Test
    fun blocksUnsupportedSchemesAndCredentials() {
        assertThrows(CrawlException::class.java) {
            NetworkSafety.validatePublicWebUri(URI("file:///etc/passwd"))
        }
        assertThrows(CrawlException::class.java) {
            NetworkSafety.validatePublicWebUri(URI("https://user:password@example.com/"))
        }
    }

    @Test
    fun classifiesLiteralPublicAndPrivateAddresses() {
        assertFalse(NetworkSafety.isBlockedAddress(InetAddress.getByName("93.184.216.34")))
        assertTrue(NetworkSafety.isBlockedAddress(InetAddress.getByName("192.168.1.1")))
    }
}
