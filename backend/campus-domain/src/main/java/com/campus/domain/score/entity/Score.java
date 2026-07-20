package com.campus.domain.score.entity;

import com.campus.common.exception.BusinessException;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Score {

    private Long id;
    private Long courseId;
    private Long studentUserId;
    private Integer score;
    /** 等级：优秀/良好/中等/及格/不及格 */
    private String grade;
    /** 类型：EXAM-考试 USUAL-平时 */
    private String type;
    /** 考试时间 */
    private LocalDate examTime;
    /** 修改原因 */
    private String reason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ========== 常量 ==========

    public static final String TYPE_EXAM = "EXAM";
    public static final String TYPE_USUAL = "USUAL";

    // ========== 业务方法 ==========

    public boolean isPassed() {
        return score != null && score >= 60;
    }

    public String getTypeDesc() {
        if (TYPE_EXAM.equals(type)) return "考试";
        if (TYPE_USUAL.equals(type)) return "平时";
        return type;
    }

    /**
     * 根据分数计算等级
     */
    public static String calcGrade(int score) {
        if (score >= 90) return "优秀";
        if (score >= 80) return "良好";
        if (score >= 70) return "中等";
        if (score >= 60) return "及格";
        return "不及格";
    }

    /**
     * 根据分数计算GPA绩点（4.0制）
     */
    public static double calcGpaPoint(int score) {
        if (score >= 90) return 4.0;
        if (score >= 80) return 3.0;
        if (score >= 70) return 2.0;
        if (score >= 60) return 1.0;
        return 0.0;
    }

    // ========== 工厂方法 ==========

    /**
     * 录入成绩
     */
    public static Score create(Long courseId, Long studentUserId, Integer score, String type, LocalDate examTime) {
        validateScore(score);
        Score s = new Score();
        s.setCourseId(courseId);
        s.setStudentUserId(studentUserId);
        s.setScore(score);
        s.setGrade(calcGrade(score));
        s.setType(type != null ? type : TYPE_EXAM);
        s.setExamTime(examTime);
        s.setCreateTime(LocalDateTime.now());
        s.setUpdateTime(LocalDateTime.now());
        return s;
    }

    /**
     * 修改成绩
     */
    public void updateScore(Integer newScore, String reason) {
        validateScore(newScore);
        this.score = newScore;
        this.grade = calcGrade(newScore);
        this.reason = reason;
        this.updateTime = LocalDateTime.now();
    }

    // ========== 校验方法 ==========

    private static void validateScore(Integer score) {
        if (score == null || score < 0 || score > 100) {
            throw new BusinessException("成绩必须在0-100之间");
        }
    }

    /**
     * 校验类型合法
     */
    public static void validateType(String type) {
        if (type == null || (!TYPE_EXAM.equals(type) && !TYPE_USUAL.equals(type))) {
            throw new BusinessException("成绩类型不合法，仅支持 EXAM 或 USUAL");
        }
    }
}
