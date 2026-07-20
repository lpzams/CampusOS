package com.campus.application.score.service;

import com.campus.application.score.command.ScoreListQuery;
import com.campus.application.score.dto.*;
import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import com.campus.domain.score.entity.Course;
import com.campus.domain.score.entity.Score;
import com.campus.domain.score.repository.CourseRepository;
import com.campus.domain.score.repository.ScoreRepository;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.entity.UserProfile;
import com.campus.domain.user.repository.UserProfileRepository;
import com.campus.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreAppService {

    private final ScoreRepository scoreRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 6.1 获取成绩列表 ====================

    public ScoreListResponseDTO getScoreList(Long userId, ScoreListQuery query) {
        // 1. 查用户
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 2. 查成绩
        List<Score> scores = scoreRepository.findByStudentUserId(userId, query.getSemester(), query.getType());

        // 3. 计算 GPA
        double gpa = calculateSemesterGpa(scores);
        int totalCredits = 0;
        int passedCredits = 0;

        // 4. 构建成绩列表
        List<ScoreItemDTO> scoreItems = new ArrayList<>();
        for (Score score : scores) {
            Course course = courseRepository.findById(score.getCourseId());
            int credit = course != null ? (course.getCredit() != null ? course.getCredit() : 0) : 0;
            totalCredits += credit;
            if (score.isPassed()) {
                passedCredits += credit;
            }

            scoreItems.add(ScoreItemDTO.builder()
                    .courseName(course != null ? course.getName() : "未知课程")
                    .courseCode(course != null ? course.getCourseCode() : "")
                    .credit(credit)
                    .score(score.getScore())
                    .grade(score.getGrade())
                    .type(score.getTypeDesc())
                    .examTime(score.getExamTime() != null ? score.getExamTime().toString() : null)
                    .isPassed(score.isPassed())
                    .build());
        }

        return ScoreListResponseDTO.builder()
                .semester(query.getSemester())
                .gpa(Math.round(gpa * 100.0) / 100.0)
                .totalCredits(totalCredits)
                .passedCredits(passedCredits)
                .scores(scoreItems)
                .build();
    }

    // ==================== 6.2 获取GPA ====================

    public GpaResponseDTO getGpa(Long userId) {
        // 1. 查全部成绩
        List<Score> allScores = scoreRepository.findAllByStudentUserId(userId);

        if (allScores.isEmpty()) {
            return GpaResponseDTO.builder()
                    .overallGpa(0.0)
                    .currentSemesterGpa(0.0)
                    .totalCredits(0)
                    .passedCredits(0)
                    .failedCredits(0)
                    .history(Collections.emptyList())
                    .build();
        }

        // 2. 计算总GPA
        double overallGpa = calculateSemesterGpa(allScores);

        // 3. 按学期分组
        Map<String, List<Score>> semesterMap = new LinkedHashMap<>();
        for (Score score : allScores) {
            Course course = courseRepository.findById(score.getCourseId());
            String semester = course != null ? course.getSemester() : "未知";
            semesterMap.computeIfAbsent(semester, k -> new ArrayList<>()).add(score);
        }

        // 4. 当前学期GPA（取按学期排序后的最后一个学期）
        List<String> sortedSemesters = new ArrayList<>(semesterMap.keySet());
        String currentSemester = sortedSemesters.isEmpty() ? null : sortedSemesters.get(sortedSemesters.size() - 1);
        double currentGpa = currentSemester != null ?
                calculateSemesterGpa(semesterMap.get(currentSemester)) : 0.0;

        // 5. 统计
        int totalCredits = 0, passedCredits = 0, failedCredits = 0;
        for (Score score : allScores) {
            Course course = courseRepository.findById(score.getCourseId());
            int credit = course != null ? (course.getCredit() != null ? course.getCredit() : 0) : 0;
            totalCredits += credit;
            if (score.isPassed()) {
                passedCredits += credit;
            } else {
                failedCredits += credit;
            }
        }

        // 6. 历史GPA
        List<GpaResponseDTO.GpaHistoryDTO> history = new ArrayList<>();
        for (Map.Entry<String, List<Score>> entry : semesterMap.entrySet()) {
            double semGpa = calculateSemesterGpa(entry.getValue());
            int semCredits = 0;
            for (Score s : entry.getValue()) {
                Course c = courseRepository.findById(s.getCourseId());
                semCredits += c != null ? (c.getCredit() != null ? c.getCredit() : 0) : 0;
            }
            history.add(GpaResponseDTO.GpaHistoryDTO.builder()
                    .semester(entry.getKey())
                    .gpa(Math.round(semGpa * 100.0) / 100.0)
                    .credits(semCredits)
                    .build());
        }

        return GpaResponseDTO.builder()
                .overallGpa(Math.round(overallGpa * 100.0) / 100.0)
                .currentSemesterGpa(Math.round(currentGpa * 100.0) / 100.0)
                .semester(currentSemester)
                .totalCredits(totalCredits)
                .passedCredits(passedCredits)
                .failedCredits(failedCredits)
                .history(history)
                .build();
    }

    // ==================== 6.3 成绩统计分析 ====================

    public ScoreStatisticsDTO getScoreStatistics(Long userId) {
        // 1. 查全部成绩
        List<Score> allScores = scoreRepository.findAllByStudentUserId(userId);

        if (allScores.isEmpty()) {
            return ScoreStatisticsDTO.builder()
                    .totalCourses(0)
                    .avgScore(0.0)
                    .maxScore(0)
                    .minScore(0)
                    .distribution(new LinkedHashMap<>())
                    .bySemester(Collections.emptyList())
                    .byCategory(new LinkedHashMap<>())
                    .build();
        }

        // 2. 基本统计
        int totalCourses = allScores.size();
        double avgScore = allScores.stream().mapToInt(Score::getScore).average().orElse(0.0);
        int maxScore = allScores.stream().mapToInt(Score::getScore).max().orElse(0);
        int minScore = allScores.stream().mapToInt(Score::getScore).min().orElse(0);

        // 3. 成绩分布
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("优秀", 0);
        distribution.put("良好", 0);
        distribution.put("中等", 0);
        distribution.put("及格", 0);
        distribution.put("不及格", 0);
        for (Score score : allScores) {
            String grade = score.getGrade();
            distribution.merge(grade, 1, Integer::sum);
        }

        // 4. 按学期统计
        Map<String, List<Score>> semesterMap = new LinkedHashMap<>();
        for (Score score : allScores) {
            Course course = courseRepository.findById(score.getCourseId());
            String semester = course != null ? course.getSemester() : "未知";
            semesterMap.computeIfAbsent(semester, k -> new ArrayList<>()).add(score);
        }

        List<ScoreStatisticsDTO.SemesterStatDTO> bySemester = new ArrayList<>();
        for (Map.Entry<String, List<Score>> entry : semesterMap.entrySet()) {
            double semAvg = entry.getValue().stream().mapToInt(Score::getScore).average().orElse(0.0);
            int semCredits = 0;
            for (Score s : entry.getValue()) {
                Course c = courseRepository.findById(s.getCourseId());
                semCredits += c != null ? (c.getCredit() != null ? c.getCredit() : 0) : 0;
            }
            bySemester.add(ScoreStatisticsDTO.SemesterStatDTO.builder()
                    .semester(entry.getKey())
                    .avgScore(Math.round(semAvg * 10.0) / 10.0)
                    .credit(semCredits)
                    .build());
        }

        // 5. 按类别统计（简化：按课程编码前缀分类）
        Map<String, List<Score>> categoryMap = new LinkedHashMap<>();
        for (Score score : allScores) {
            Course course = courseRepository.findById(score.getCourseId());
            String category = "公共课";
            if (course != null && course.getCourseCode() != null) {
                if (course.getCourseCode().startsWith("CS") || course.getCourseCode().startsWith("SE")) {
                    category = "专业课";
                } else if (course.getCourseCode().startsWith("MATH") || course.getCourseCode().startsWith("ENG")) {
                    category = "公共课";
                } else {
                    category = "选修课";
                }
            }
            categoryMap.computeIfAbsent(category, k -> new ArrayList<>()).add(score);
        }

        Map<String, Double> byCategory = new LinkedHashMap<>();
        for (Map.Entry<String, List<Score>> entry : categoryMap.entrySet()) {
            double catAvg = entry.getValue().stream().mapToInt(Score::getScore).average().orElse(0.0);
            byCategory.put(entry.getKey(), Math.round(catAvg * 10.0) / 10.0);
        }

        return ScoreStatisticsDTO.builder()
                .totalCourses(totalCourses)
                .avgScore(Math.round(avgScore * 10.0) / 10.0)
                .maxScore(maxScore)
                .minScore(minScore)
                .distribution(distribution)
                .bySemester(bySemester)
                .byCategory(byCategory)
                .build();
    }

    // ==================== 私有方法 ====================

    /**
     * 计算一批成绩的加权GPA
     */
    private double calculateSemesterGpa(List<Score> scores) {
        if (scores == null || scores.isEmpty()) return 0.0;

        double totalPoints = 0.0;
        int totalCredits = 0;

        for (Score score : scores) {
            Course course = courseRepository.findById(score.getCourseId());
            int credit = course != null ? (course.getCredit() != null ? course.getCredit() : 0) : 0;
            totalPoints += Score.calcGpaPoint(score.getScore()) * credit;
            totalCredits += credit;
        }

        return totalCredits > 0 ? totalPoints / totalCredits : 0.0;
    }
}
