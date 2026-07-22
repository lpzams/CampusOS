package com.campus.application.course;

import com.campus.common.exception.BusinessException;
import com.campus.domain.course.selection.CourseSelectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CourseSelectionAppService {
    private final CourseSelectionRepository repository;

    public CourseSelectionAppService(CourseSelectionRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> mine(Long userId) {
        return repository.findByUserId(userId);
    }

    @Transactional
    public void select(Long userId, String courseSourceId) {
        String sourceId = requiredSourceId(courseSourceId);
        if (!repository.catalogExists(sourceId)) throw new BusinessException(404, "课程不存在或尚未导入评价目录");
        if (!repository.select(userId, sourceId)) throw new BusinessException(400, "该课程已在我的选课列表中");
    }

    @Transactional
    public void cancel(Long userId, String courseSourceId) {
        if (!repository.cancel(userId, requiredSourceId(courseSourceId))) {
            throw new BusinessException(404, "未找到该选课记录");
        }
    }

    private String requiredSourceId(String courseSourceId) {
        if (courseSourceId == null || courseSourceId.isBlank()) throw new BusinessException(400, "课程标识不能为空");
        return courseSourceId.trim();
    }
}
