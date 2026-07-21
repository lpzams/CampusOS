package com.campus.infrastructure.cache;

import com.campus.domain.notice.entity.Notice;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 公告缓存服务 —— Cache-Aside 模式
 * <p>
 * 仅对公告详情做缓存，列表直接查 DB
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String NOTICE_INFO_PREFIX = "notice:info:";
    private static final int NOTICE_INFO_EXPIRE_SECONDS = 1800; // 30分钟

    /**
     * 从缓存获取公告
     */
    public Notice getNotice(Long noticeId) {
        if (noticeId == null) return null;
        String key = NOTICE_INFO_PREFIX + noticeId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, Notice.class);
        } catch (Exception e) {
            log.warn("反序列化公告缓存失败: key={}", key);
            stringRedisTemplate.delete(key);
            return null;
        }
    }

    /**
     * 回填公告缓存
     */
    public void putNotice(Notice notice) {
        if (notice == null || notice.getId() == null) return;
        try {
            String json = objectMapper.writeValueAsString(notice);
            stringRedisTemplate.opsForValue().set(
                    NOTICE_INFO_PREFIX + notice.getId(),
                    json,
                    NOTICE_INFO_EXPIRE_SECONDS,
                    TimeUnit.SECONDS
            );
            log.debug("回填公告缓存: noticeId={}", notice.getId());
        } catch (JsonProcessingException e) {
            log.error("序列化 Notice 失败: noticeId={}", notice.getId(), e);
        }
    }

    /**
     * 删除公告缓存
     */
    public void evictNotice(Long noticeId) {
        if (noticeId == null) return;
        stringRedisTemplate.delete(NOTICE_INFO_PREFIX + noticeId);
        log.debug("删除公告缓存: noticeId={}", noticeId);
    }
}
