package com.campus.application.common.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 文件上传响应
 */
@Data
@Builder
public class FileUploadDTO {

    /** 文件访问 URL */
    private String fileUrl;

    /** 文件唯一标识 */
    private String fileId;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 原始文件名 */
    private String fileName;
}
