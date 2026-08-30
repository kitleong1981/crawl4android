# Security

## Reporting

Please report a suspected vulnerability through GitHub's private security
advisory feature instead of opening a public issue.

## Boundaries

Crawl4Android accepts untrusted URLs and page text. It blocks non-HTTP schemes,
URL credentials, localhost, private, link-local, carrier-grade NAT, multicast,
and IPv6 unique-local destinations before every request and redirect. It also
limits redirects, response size, accepted content types, and timeouts.

These checks are defense in depth, not a complete sandbox. DNS rebinding,
platform networking behavior, proxy configuration, and future protocol edge
cases can affect enforcement. Do not expose the library as an unrestricted
server-side fetch proxy.

Extracted text is untrusted model input. The generated context marks source
text as data, but this cannot guarantee resistance to prompt injection. Host
applications should expose citations and avoid granting models sensitive tools
based solely on crawled instructions.
