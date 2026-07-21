package com.campus.infrastructure.file;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.campus.common.exception.BusinessException;
import com.campus.infrastructure.config.OssProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云 OSS 文件存储
 */
@Slf4j
@Component
public class OssFileService implements FileService {

    private final OssProperties ossProperties;
    private OSS ossClient;

    public OssFileService(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    @PostConstruct
    public void init() {
        this.ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
        log.info("OSS 客户端初始化完成: bucket={}, endpoint={}",
                ossProperties.getBucketName(), ossProperties.getEndpoint());
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSS 客户端已关闭");
        }
    }

    @Override
    public String upload(InputStream inputStream, String originalFilename, String dir) {
        if (inputStream == null) {
            throw new BusinessException("上传文件不能为空");
        }

        String bucketName = ossProperties.getBucketName();

        // 1. 生成 OSS 存储路径: dir/20240615/uuid.ext
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = dir + "/" + dateStr + "/"
                + UUID.randomUUID().toString().replace("-", "") + extension;

        // 2. 上传到 OSS
        try {
            PutObjectRequest request = new PutObjectRequest(bucketName, objectName, inputStream);
            ossClient.putObject(request);
        } catch (Exception e) {
            log.error("OSS 上传失败: bucket={}, object={}", bucketName, objectName, e);
            throw new BusinessException("文件上传失败，请稍后重试");
        }

        // 3. 返回可访问 URL（CDN 域名或 Bucket 域名）
        String domain = ossProperties.getDomain();
        if (domain == null || domain.isEmpty()) {
            domain = "https://" + bucketName + "." + ossProperties.getEndpoint();
        }
        String fileUrl =  domain + "/" + objectName;

        log.info("OSS 上传成功: {} -> {}", originalFilename, fileUrl);
        return fileUrl;
    }
}
