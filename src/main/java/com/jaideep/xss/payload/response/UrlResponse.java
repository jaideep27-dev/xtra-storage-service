package com.jaideep.xss.payload.response;

import java.time.Instant;

public record UrlResponse(
        Long id,
        String shortUrl,
        String originalUrl,
        Instant expirationTime
) {

}
