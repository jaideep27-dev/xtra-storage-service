package com.jaideep.xss.controller;

import com.jaideep.xss.payload.response.FileResponse;
import com.jaideep.xss.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController("/api/v1/xtra/storage")
@Slf4j
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;

    @PostMapping(value = "/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> uploadFile(@RequestPart String dir, @RequestPart("file") MultipartFile file) {
        try {
            log.info("Received request to upload file : {}", file.getName());
            FileResponse fileResponse = storageService.uploadFile(dir, file);
            log.info("File uploaded successfully");
            return ResponseEntity.ok(fileResponse);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return ResponseEntity.internalServerError().build();
    }

    @GetMapping("/getFileUrl")
    ResponseEntity<?> getFileUrl(@RequestHeader("filePath") String filePath) {
        try {
            log.info("Received request to get file url for : {}", filePath);
            String fileUrl = storageService.getFileUrl(filePath);
            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return ResponseEntity.internalServerError().build();
    }

    @DeleteMapping("/delete")
    ResponseEntity<?> deleteFile(@RequestHeader("filePath") String filePath) {
        try {
            log.info("Received request to delete file : {}", filePath);
            storageService.deleteFile(filePath);
            log.info("File deleted successfully");
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return ResponseEntity.internalServerError().build();
    }
}
