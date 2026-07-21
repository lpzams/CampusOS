package com.campus.api.controller;

import com.campus.application.common.dto.FileUploadDTO;
import com.campus.common.api.Result;
import com.campus.common.exception.BusinessException;
import com.campus.infrastructure.config.FileUploadProperties;
import com.campus.infrastructure.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口控制器（跨模块共享）
 */
@Slf4j
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
@Tag(name = "通用接口", description = "文件上传等跨模块通用接口")
public class CommonController {

    private final FileService fileService;
    private final FileUploadProperties uploadProperties;

    @PostMapping("/upload/image")
    @Operation(summary = "上传图片")
    public Result<FileUploadDTO> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "module", defaultValue = "common") String module) throws IOException {

        // 1. 文件非空校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 2. 文件大小校验
        if (file.getSize() > uploadProperties.getMaxImageSize()) {
            long maxMB = uploadProperties.getMaxImageSize() / 1024 / 1024;
            throw new BusinessException("文件大小不能超过 " + maxMB + "MB");
        }

        // 3. 文件类型校验
        String contentType = file.getContentType();
        if (contentType == null || !uploadProperties.getAllowedImageTypes().contains(contentType)) {
            throw new BusinessException("仅支持 " + uploadProperties.getAllowedImageTypes() + " 格式的图片");
        }

        // 4. 模块名校验（防目录穿越）
        if (!uploadProperties.getAllowedModules().contains(module)) {
            log.warn("无效的模块标识: {}，使用默认值 common", module);
            module = "common";
        }

        // 5. 上传到 OSS
        String fileUrl = fileService.upload(
                file.getInputStream(),
                file.getOriginalFilename(),
                module
        );

        // 6. 生成 fileId
        String fileId = "file_" + System.currentTimeMillis() + "_" +
                UUID.randomUUID().toString().substring(0, 8);

        // 7. 返回
        FileUploadDTO dto = FileUploadDTO.builder()
                .fileUrl(fileUrl)
                .fileId(fileId)
                .fileSize(file.getSize())
                .fileName(file.getOriginalFilename())
                .build();

        log.info("图片上传成功: module={}, fileUrl={}, size={}",
                module, fileUrl, file.getSize());
        return Result.success(dto);
    }
}
