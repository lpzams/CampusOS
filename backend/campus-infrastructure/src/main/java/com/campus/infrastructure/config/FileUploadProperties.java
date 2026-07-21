package com.campus.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 文件上传配置（application.yml 中 file.upload.* 统一管理）
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties {

    /** 允许的图片 MIME 类型 */
    private Set<String> allowedImageTypes = Set.of("image/jpeg", "image/png", "image/webp");

    /** 图片最大大小（字节），默认 5MB */
    private long maxImageSize = 5 * 1024 * 1024;

    /** 允许的模块标识 */
    private Set<String> allowedModules = Set.of("avatar", "news", "product", "activity", "repair", "common");
}
