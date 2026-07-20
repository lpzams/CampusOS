package com.campus.application.repair.service;

import com.campus.application.user.service.UserAppService;
import com.campus.common.api.PageResult;
import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import com.campus.domain.repair.entity.*;
import com.campus.domain.repair.repository.RepairRepository;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepairAppService {

    private final RepairRepository repairRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 11.1 提交报修 ====================

    @Transactional
    public Map<String, Object> submitRepair(Long userId, Map<String, Object> command) {
        User user = userRepository.findById(userId);
        String type = (String) command.get("type");
        String typeCode = Repair.typeNameToCode(type);
        String images = command.get("images") != null ? command.get("images").toString() : null;

        Repair repair = Repair.create(userId, type, typeCode,
                (String) command.get("title"),
                (String) command.get("description"),
                images,
                (String) command.get("building"),
                (String) command.get("room"),
                (String) command.get("contactPhone"));
        repair.setExpectedTime(LocalDateTime.now().plusDays(1));
        repairRepository.save(repair);

        // 添加进度：已提交
        repairRepository.saveProgress(RepairProgress.create(repair.getId(), "PENDING", "报修申请已提交"));

        log.info("报修提交: repairId={}, title={}", repair.getId(), repair.getTitle());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("repairId", repair.getId());
        result.put("title", repair.getTitle());
        result.put("status", "待接单");
        result.put("statusCode", "PENDING");
        result.put("createTime", repair.getCreateTime().format(FORMATTER));
        result.put("expectedTime", repair.getExpectedTime().format(FORMATTER));
        return result;
    }

    // ==================== 11.2 获取报修列表 ====================

    public List<Map<String, Object>> getRepairList(Long userId) {
        List<Repair> repairs = repairRepository.findByUserId(userId);
        return repairs.stream().map(r -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.getId());
            item.put("title", r.getTitle());
            item.put("type", r.getType());
            item.put("typeCode", r.getTypeCode());
            item.put("status", r.getStatusDesc());
            item.put("statusCode", r.getStatus());
            item.put("statusDesc", r.getStatusDesc());
            item.put("createTime", r.getCreateTime() != null ? r.getCreateTime().format(FORMATTER) : null);
            item.put("expectedTime", r.getExpectedTime() != null ? r.getExpectedTime().format(FORMATTER) : null);
            item.put("handler", r.getHandler());
            item.put("handlerPhone", r.getHandlerPhone());
            item.put("completeTime", r.getCompleteTime() != null ? r.getCompleteTime().format(FORMATTER) : null);
            RepairEvaluation eval = repairRepository.findEvaluationByRepairId(r.getId());
            item.put("isEvaluated", eval != null);
            return item;
        }).collect(Collectors.toList());
    }

    // ==================== 11.3 获取报修详情 ====================

    public Map<String, Object> getRepairDetail(Long repairId) {
        Repair repair = repairRepository.findById(repairId);
        if (repair == null) throw new BusinessException(ResultCode.REPAIR_NOT_FOUND);

        List<RepairProgress> progressList = repairRepository.findProgressByRepairId(repairId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", repair.getId());
        result.put("title", repair.getTitle());
        result.put("type", repair.getType());
        result.put("typeCode", repair.getTypeCode());
        result.put("description", repair.getDescription());
        result.put("images", parseImages(repair.getImages()));
        result.put("building", repair.getBuilding());
        result.put("room", repair.getRoom());
        result.put("contactPhone", repair.getContactPhone());
        result.put("status", repair.getStatusDesc());
        result.put("statusCode", repair.getStatus());
        result.put("statusDesc", repair.getStatusDesc());
        result.put("createTime", repair.getCreateTime() != null ? repair.getCreateTime().format(FORMATTER) : null);
        result.put("updateTime", repair.getUpdateTime() != null ? repair.getUpdateTime().format(FORMATTER) : null);
        result.put("expectedTime", repair.getExpectedTime() != null ? repair.getExpectedTime().format(FORMATTER) : null);
        result.put("handler", repair.getHandler());
        result.put("handlerPhone", repair.getHandlerPhone());

        List<Map<String, Object>> progress = new ArrayList<>();
        for (RepairProgress p : progressList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("time", p.getCreateTime() != null ? p.getCreateTime().format(FORMATTER) : null);
            item.put("status", p.getStatus());
            item.put("content", p.getContent());
            progress.add(item);
        }
        result.put("progress", progress);
        return result;
    }

    // ==================== 11.4 评价维修服务 ====================

    @Transactional
    public Map<String, Object> evaluateRepair(Long userId, Map<String, Object> command) {
        Long repairId = ((Number) command.get("repairId")).longValue();
        Repair repair = repairRepository.findById(repairId);
        if (repair == null) throw new BusinessException(ResultCode.REPAIR_NOT_FOUND);
        if (repairRepository.findEvaluationByRepairId(repairId) != null) {
            throw new BusinessException(ResultCode.REPAIR_ALREADY_EVALUATED);
        }

        int score = ((Number) command.get("score")).intValue();
        String content = (String) command.get("content");
        RepairEvaluation evaluation = RepairEvaluation.create(repairId, userId, score, content);
        repairRepository.saveEvaluation(evaluation);

        log.info("报修评价: repairId={}, score={}", repairId, score);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("repairId", repairId);
        result.put("score", score);
        result.put("content", content);
        result.put("evaluateTime", evaluation.getCreateTime().format(FORMATTER));
        return result;
    }

    // ==================== 11.5 报修列表（管理员） ====================

    public PageResult<Map<String, Object>> getAdminRepairList(String status, int page, int size) {
        int offset = (page - 1) * size;
        List<Repair> repairs = repairRepository.findAll(status, offset, size);
        long total = repairRepository.count(status);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Repair r : repairs) {
            User user = userRepository.findById(r.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.getId());
            item.put("title", r.getTitle());
            item.put("type", r.getType());
            item.put("building", r.getBuilding());
            item.put("room", r.getRoom());
            item.put("submitter", user != null ? user.getRealName() : "未知");
            item.put("submitterPhone", r.getContactPhone());
            item.put("status", r.getStatus());
            item.put("statusDesc", r.getStatusDesc());
            item.put("handler", r.getHandler());
            item.put("completeTime", r.getCompleteTime() != null ? r.getCompleteTime().format(FORMATTER) : null);
            item.put("createTime", r.getCreateTime() != null ? r.getCreateTime().format(FORMATTER) : null);
            list.add(item);
        }

        return PageResult.of(total, list, page, size);
    }

    // ==================== 11.6 更新报修状态（管理员） ====================

    @Transactional
    public Map<String, Object> updateRepairStatus(Long repairId, Map<String, Object> command) {
        Repair repair = repairRepository.findById(repairId);
        if (repair == null) throw new BusinessException(ResultCode.REPAIR_NOT_FOUND);

        String oldStatus = repair.getStatus();
        String newStatus = (String) command.get("status");
        String handler = (String) command.get("handler");
        String handlerPhone = (String) command.get("handlerPhone");

        repair.updateStatus(newStatus, handler, handlerPhone);
        repairRepository.update(repair);

        // 添加进度
        String statusDesc = repair.getStatusDesc();
        String progressContent = Repair.STATUS_PROCESSING.equals(newStatus) ? "维修师傅已接单" :
                Repair.STATUS_COMPLETED.equals(newStatus) ? "已维修完成" : "已取消";
        repairRepository.saveProgress(RepairProgress.create(repairId, newStatus, progressContent));

        log.info("报修状态更新: repairId={}, {} -> {}", repairId, oldStatus, newStatus);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("repairId", repairId);
        result.put("title", repair.getTitle());
        result.put("oldStatus", oldStatus);
        result.put("newStatus", newStatus);
        result.put("statusDesc", repair.getStatusDesc());
        result.put("handler", repair.getHandler());
        result.put("updateTime", repair.getUpdateTime().format(FORMATTER));
        return result;
    }

    // ==================== 私有方法 ====================

    private List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) return Collections.emptyList();
        return Arrays.asList(imagesJson.replaceAll("[\\[\\]\"]", "").split(",\\s*"));
    }
}
