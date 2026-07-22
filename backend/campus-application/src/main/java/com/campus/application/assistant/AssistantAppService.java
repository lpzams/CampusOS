package com.campus.application.assistant;

import com.campus.application.shared.CampusAppService;
import com.campus.common.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AssistantAppService {
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final Map<String, String> HOT_ANSWERS = Map.of(
            "如何申请奖学金？", "请进入“学生事务”查看当学年奖学金通知，按要求准备成绩单、申请表和相关证明，在截止日期前提交给辅导员审核。具体条件和时间以学校最新通知为准。",
            "在哪里查看本周课表？", "进入 CampusOS 的“课程表”页面即可查看本周课程；桌面端也可以打开“课程表.app”快速查看今日课程。",
            "如何查询空闲教室？", "进入“空闲教室”页面，选择日期、教学楼和时间段后查询即可。教室状态可能临时调整，使用前请再次确认。",
            "校园卡丢失后怎么挂失？", "进入“校园卡”页面点击“挂失”，确认后校园卡会立即停止消费。找到卡后可在同一页面解挂；无法找回时请前往校园卡服务中心补办。",
            "宿舍报修需要准备什么？", "请准备宿舍楼栋、房间号、故障描述、联系电话和清晰照片，然后在“宿舍报修”页面提交。紧急漏水或用电故障请同时联系宿管。",
            "活动报名后如何签到？", "活动开始后进入“我的活动”，打开对应活动并按现场要求签到。部分活动需要扫描工作人员提供的签到码，请以活动通知为准。"
    );
    private final CampusAppService records;
    private final ObjectMapper json;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    public AssistantAppService(CampusAppService records, ObjectMapper json,
            @Value("${campus.ai.base-url:}") String baseUrl,
            @Value("${campus.ai.api-key:}") String apiKey,
            @Value("${campus.ai.model:claude-sonnet-4-5}") String model) {
        this.records = records;
        this.json = json;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.apiKey = apiKey;
        this.model = model;
    }

    public Map<String, Object> chat(Long userId, Map<String, Object> body) {
        if (apiKey.isBlank() || baseUrl.isBlank()) throw new BusinessException(503, "AI 服务尚未配置，请设置 ANTHROPIC_AUTH_TOKEN");
        String question = String.valueOf(body.getOrDefault("question", "")).trim();
        List<Map<String, Object>> images = maps(body.get("images"));
        if (question.isBlank() && images.isEmpty()) throw new BusinessException(400, "问题或图片不能同时为空");
        if (question.length() > 10_000) throw new BusinessException(400, "问题不能超过 10000 字");
        if (images.size() > 4) throw new BusinessException(400, "一次最多发送 4 张图片");

        List<Map<String, Object>> messages = new ArrayList<>();
        for (Map<String, Object> item : maps(body.get("history")).stream().limit(20).toList()) {
            String role = String.valueOf(item.get("role"));
            String content = String.valueOf(item.getOrDefault("content", "")).trim();
            if ((role.equals("user") || role.equals("assistant")) && !content.isBlank())
                messages.add(Map.of("role", role, "content", content.substring(0, Math.min(content.length(), 10_000))));
        }

        List<Map<String, Object>> content = new ArrayList<>();
        int imageBytes = 0;
        for (Map<String, Object> image : images) {
            String mediaType = String.valueOf(image.get("mediaType"));
            String data = String.valueOf(image.get("data"));
            if (!IMAGE_TYPES.contains(mediaType)) throw new BusinessException(400, "仅支持 JPG、PNG、GIF、WebP 图片");
            try { imageBytes += Base64.getDecoder().decode(data).length; }
            catch (IllegalArgumentException e) { throw new BusinessException(400, "图片数据无效"); }
            content.add(Map.of("type", "image", "source", Map.of("type", "base64", "media_type", mediaType, "data", data)));
        }
        if (imageBytes > 15 * 1024 * 1024) throw new BusinessException(400, "图片总大小不能超过 15MB");
        content.add(Map.of("type", "text", "text", question.isBlank() ? "请分析这些图片。" : question));
        messages.add(Map.of("role", "user", "content", content));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("max_tokens", 2048);
        payload.put("system", "你是 CampusOS 智慧校园助手。用中文准确、简洁地回答；看不清图片或不确定时明确说明，不编造校园政策。允许分析用户发送的图片。");
        payload.put("messages", messages);
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/v1/messages"))
                    .timeout(Duration.ofSeconds(90)).header("Content-Type", "application/json")
                    .header("anthropic-version", "2023-06-01").header("x-api-key", apiKey)
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload))).build();
            HttpResponse<String> response = sendWithRetry(request);
            Map<String, Object> result = json.readValue(response.body(), new TypeReference<>() {});
            if (response.statusCode() / 100 != 2)
                throw new BusinessException(502, "Claude 调用失败：" + String.valueOf(result.getOrDefault("error", response.statusCode())));
            String answer = maps(result.get("content")).stream().filter(it -> "text".equals(it.get("type")))
                    .map(it -> String.valueOf(it.getOrDefault("text", ""))).reduce("", (a, b) -> a + b).trim();
            if (answer.isBlank()) throw new BusinessException(502, "Claude 未返回文本内容");
            return Map.of("answer", answer, "relatedQuestions", List.of());
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(502, "Claude 调用被中断");
        } catch (Exception e) {
            throw new BusinessException(502, "无法连接 Claude 服务：" + e.getMessage());
        }
    }

    private HttpResponse<String> sendWithRetry(HttpRequest request) throws IOException, InterruptedException {
        IOException lastError = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                if (attempt == 2 || (response.statusCode() != 429 && response.statusCode() / 100 != 5)) return response;
            } catch (IOException e) {
                lastError = e;
                if (attempt == 2) throw e;
            }
            Thread.sleep(500L * (attempt + 1));
        }
        throw lastError;
    }

    public List<Map<String, Object>> hotQuestions() {
        return records.list("hotQuestion").stream().map(item -> {
            Map<String, Object> result = new LinkedHashMap<>(item);
            result.put("answer", HOT_ANSWERS.getOrDefault(String.valueOf(item.get("question")), "请以 CampusOS 对应功能页面和学校最新通知为准。"));
            return result;
        }).toList();
    }
    public Map<String, Object> process(String type, String action) {
        if (type == null || type.isBlank() || action == null || action.isBlank()) throw new BusinessException(400, "流程类型和事项不能为空");
        return Map.of("title", action + "办理流程", "steps", List.of("准备相关证件", "前往服务中心或线上提交", "等待审核结果"));
    }
    public List<Map<String, Object>> recommend(Long userId, String type) { return records.search("recommendation", "type", type); }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> maps(Object value) {
        if (!(value instanceof List<?> list)) return List.of();
        return list.stream().filter(Map.class::isInstance).map(it -> (Map<String, Object>) it).toList();
    }
}
