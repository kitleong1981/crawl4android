# Changelog

All notable changes to this project are documented here.

## 0.1.1 - 2026-08-30

- Add ordered search-provider fallback without bypassing verification challenges.
- Add Mojeek and Bing server-rendered search providers.
- Keep DuckDuckGo Lite as the first provider while improving behavior when it is rate-limited.
- Add parser and fallback unit tests.

## 0.1.0 - 2026-08-30

- Initial Android library release.
- DuckDuckGo Lite search provider.
- Guarded HTTP client with redirect, size, type, timeout, and private-network restrictions.
- Static HTML and plain-text extraction.
- English token and CJK bigram BM25 ranking.
- Bounded, citation-oriented context generation for local LLM prompts.
