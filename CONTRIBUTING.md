# Contributing

Issues and pull requests are welcome.

## Local checks

Use JDK 17 and an Android SDK containing API 35:

```bash
./gradlew test lint assembleRelease
```

Keep network tests deterministic. Parser tests should use checked-in HTML
fixtures rather than depend on a live search response.

By submitting a contribution, you agree that it is licensed under Apache-2.0.
