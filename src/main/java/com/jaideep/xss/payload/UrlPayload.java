package com.jaideep.xss.payload;

import java.time.Instant;

public record UrlPayload(Long id, String originalUrl, Instant expirationTime) {
}
