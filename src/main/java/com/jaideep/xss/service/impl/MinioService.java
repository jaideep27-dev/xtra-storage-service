package com.jaideep.xss.service.impl;

import com.jaideep.xss.payload.UrlPayload;
import com.jaideep.xss.payload.response.FileResponse;
import com.jaideep.xss.payload.response.UrlResponse;
import com.jaideep.xss.service.StorageService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioService implements StorageService {

    private final MinioClient minioClient;
    private final RestTemplate restTemplate;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public FileResponse uploadFile(String dir, MultipartFile file) {
        try {
            // Generate a unique file name
            String filePath = dir + "\\" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            // Check if bucket exists, create if not
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            // Upload file
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(filePath).stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build());
            // For download/viewing
            String originalUrl = this.getFileUrl(filePath);
            UrlPayload urlPayload = new UrlPayload(null, originalUrl, Instant.now().plus(7, ChronoUnit.DAYS));
            ResponseEntity<UrlResponse> response = restTemplate.postForEntity("http://localhost:9003/api/v1/xtra/url/create", urlPayload, UrlResponse.class);
            UrlResponse urlResponse = response.getBody();
            if (urlResponse == null) {
                throw new RuntimeException("Url service returned null");
            }
            return FileResponse.builder().filePath(filePath).urlId(urlResponse.id()).shortUrl(urlResponse.shortUrl()).originalUrl(urlResponse.originalUrl()).build();
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String filePath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucketName).object(filePath).method(Method.GET) // For download/viewing
                .expiry(7, TimeUnit.DAYS) // URL expires in 7 days
                .build());
    }

    public InputStream downloadFile(String fileName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String filePath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(filePath).build());
    }

}
