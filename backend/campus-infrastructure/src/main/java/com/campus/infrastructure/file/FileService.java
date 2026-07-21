package com.campus.infrastructure.file;

import java.io.InputStream;

/**
 * 文件存储服务接口
 * <p>
 * 支持本地存储 / 阿里云 OSS 等多种实现，通过 Spring Profile 切换
 */
public interface FileService {

    /**
     * 上传文件并返回可访问的 URL
     *
     * @param inputStream      文件流
     * @param originalFilename 原始文件名（用于获取扩展名）
     * @param dir              子目录（如 avatar）
     * @return 文件访问 URL
     */
    String upload(InputStream inputStream, String originalFilename, String dir);
}
