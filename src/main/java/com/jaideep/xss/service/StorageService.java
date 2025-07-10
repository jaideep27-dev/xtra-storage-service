package com.jaideep.xss.service;

import com.jaideep.xss.payload.response.FileResponse;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface StorageService {
    FileResponse uploadFile(String dir, MultipartFile file) throws Exception;

    String getFileUrl(String filename) throws InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, ServerException, io.minio.errors.ErrorResponseException;

    InputStream downloadFile(String filename) throws Exception;

    void deleteFile(String filePath) throws InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, ServerException, io.minio.errors.ErrorResponseException;

}
