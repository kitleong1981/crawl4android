# Crawl4Android

[![CI](https://github.com/kitleong1981/crawl4android/actions/workflows/ci.yml/badge.svg)](https://github.com/kitleong1981/crawl4android/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
[![Android API](https://img.shields.io/badge/minSdk-24-brightgreen.svg)](build.gradle.kts)

Android-first, bounded web research for local LLM applications.

Crawl4Android searches for candidate pages, extracts readable static content,
ranks chunks against the user's query, and returns compact prompt context plus
structured source URLs. It is native Kotlin/JVM and does not bundle Python,
Chromium, Playwright, an LLM, or UI components.

## Pipeline

1. A `SearchProvider` finds candidate pages. The default tries DuckDuckGo Lite, Mojeek, and Bing in order when an earlier provider returns a verification challenge or no usable results.
2. A guarded HTTP client downloads a bounded number of public HTTP(S) pages.
3. Static HTML chrome is removed and readable blocks are packed into chunks.
4. BM25 ranks chunks using word tokens and CJK bigrams.
5. The library returns bounded, citation-oriented context and structured sources.

## Requirements

- Android API 24+
- JDK 17 for building
- Android SDK 35 for building
- `android.permission.INTERNET` in the host application

## Add To A Project

### Source checkout

Clone Crawl4Android beside the host project and add this to the host's
`settings.gradle.kts`:

```kotlin
includeBuild("../crawl4android")
```

Then add the dependency:

```kotlin
dependencies {
    implementation("io.github.kitleong1981:crawl4android:0.1.1")
}
```

Gradle composite-build dependency substitution uses the local checkout.

### Release AAR

Download `crawl4android-0.1.1.aar` from the GitHub release, place it in the
host's `app/libs/`, and add:

```kotlin
dependencies {
    implementation(files("libs/crawl4android-0.1.1.aar"))
    implementation("org.jsoup:jsoup:1.23.2")
}
```

The direct AAR does not bundle jsoup. A conventional Maven repository is not
published yet.

## Usage

```kotlin
import io.github.kitleong1981.crawl4android.Crawl4Android
import io.github.kitleong1981.crawl4android.CrawlConfig

val crawler = Crawl4Android(
    CrawlConfig(
        maxSearchResults = 5,
        maxPagesToCrawl = 3,
        maxContextChars = 8_000
    )
)

val report = withContext(Dispatchers.IO) {
    crawler.research("What changed in Android local LLM support?")
}

localModel.send(userMessage + "\n\n" + report.context)
```

Render or append `report.sources` in the host UI so source links remain
available even when a small model fails to reproduce citation formatting.

Calls are blocking by design to avoid imposing a coroutine dependency. Invoke
`research` from an I/O dispatcher or background executor.

## Custom Search Provider

```kotlin
val provider = SearchProvider { query, limit ->
    mySearchApi.search(query, limit).map {
        SearchResult(title = it.title, url = it.url, snippet = it.snippet)
    }
}

val crawler = Crawl4Android(searchProvider = provider)
```

## Public API

- `Crawl4Android`
- `CrawlConfig`
- `CrawlReport`
- `CrawledSource`
- `CrawlProgress`
- `SearchProvider`
- `SearchResult`
- `FallbackSearchProvider`
- `DuckDuckGoLiteSearchProvider`
- `MojeekSearchProvider`
- `BingSearchProvider`
- `CrawlException`

## Limits

- DuckDuckGo Lite, Mojeek, and Bing are unofficial HTML integrations and can change or request verification. The fallback does not attempt to bypass challenges.
- JavaScript-rendered pages are not rendered; static HTML and plain text are supported.
- Search calls can be rate-limited. Host applications should avoid aggressive automation.
- URL safety checks are defense in depth, not a complete network sandbox.
- Web content is untrusted. Prompt delimiters cannot guarantee resistance to prompt injection.
- Hosts are responsible for applicable robots policies, terms, copyright, privacy, and request limits.

See [SECURITY.md](SECURITY.md) for the security model.

## Development

```bash
./gradlew test lint assembleRelease
```

## Inspiration

The extraction pipeline was inspired by Crawl4AI's documented architecture,
especially content pruning, chunking, BM25 filtering, and cited output.
Crawl4Android is an original Kotlin implementation and does not include
Crawl4AI source, Python, Playwright, or Chromium.

## License

Apache-2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
