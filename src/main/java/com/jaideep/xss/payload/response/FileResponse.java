package com.jaideep.xss.payload.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class FileResponse {
    private String filePath;
    private String shortUrl;
    private Long urlId;
    private String originalUrl;
    private Instant expirationTime;
}
