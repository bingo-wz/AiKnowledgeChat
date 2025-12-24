package com.classroom.common.controller;

import com.classroom.common.result.R;
import com.classroom.common.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final MinioService minioService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public R<FileUploadResult> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "common") String folder) {
        String objectName = minioService.uploadFile(file, folder);
        String url = minioService.getPresignedUrl(objectName);

        FileUploadResult result = new FileUploadResult();
        result.setObjectName(objectName);
        result.setUrl(url);
        result.setFileName(file.getOriginalFilename());
        result.setFileSize(file.getSize());

        return R.ok(result);
    }

    /**
     * 获取文件访问URL
     */
    @GetMapping("/url")
    public R<String> getUrl(@RequestParam String objectName) {
        return R.ok(minioService.getPresignedUrl(objectName));
    }

    /**
     * 删除文件
     */
    @DeleteMapping
    public R<Void> delete(@RequestParam String objectName) {
        minioService.deleteFile(objectName);
        return R.ok();
    }

    @lombok.Data
    public static class FileUploadResult {
        private String objectName;
        private String url;
        private String fileName;
        private Long fileSize;
    }
}
