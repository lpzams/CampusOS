package com.campus.application.score.service;

import com.campus.application.score.command.*;
import com.campus.application.score.dto.*;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.PageResult;
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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreAdminService {

    private final ScoreRepository scoreRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 6.4 录入成绩（教师） ====================

    @Transactional(rollbackFor = Exception.class)
    public EnterScoreResponseDTO enterScore(EnterScoreCommand command) {
        // 1. 校验教师权限（是该课程的授课教师或管理员）
        Long userId = UserAppService.getCurrentUserId();
        User currentUser = userRepository.findById(userId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (!currentUser.isTeacher() && !currentUser.isAdmin()) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        // 2. 校验课程存在
        Course course = courseRepository.findById(command.getCourseId());
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }

        // 3. 校验成绩类型
        Score.validateType(command.getType());

        // 4. 查找学生
        User student = userRepository.findByUsername(command.getStudentId());
        if (student == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (!student.isStudent()) {
            throw new BusinessException("该用户不是学生");
        }

        // 5. 校验是否已存在该类型成绩
        Score existing = scoreRepository.findByCourseIdAndStudentUserIdAndType(
                command.getCourseId(), student.getId(), command.getType());
        if (existing != null) {
            throw new BusinessException(ResultCode.SCORE_ALREADY_EXISTS);
        }

        // 6. 创建成绩
        Score score = Score.create(command.getCourseId(), student.getId(),
                command.getScore(), command.getType(), null);
        scoreRepository.save(score);

        log.info("成绩录入成功: courseId={}, studentId={}, score={}, type={}",
                command.getCourseId(), command.getStudentId(), command.getScore(), command.getType());

        return EnterScoreResponseDTO.builder()
                .courseId(command.getCourseId())
                .studentId(command.getStudentId())
                .score(command.getScore())
                .type(command.getType())
                .updateTime(score.getCreateTime() != null ? score.getCreateTime().format(FORMATTER) : null)
                .build();
    }

    // ==================== 6.5 批量录入成绩（教师） ====================

    @Transactional(rollbackFor = Exception.class)
    public BatchEnterScoreResponseDTO batchEnterScore(BatchEnterScoreCommand command) {
        // 1. 校验教师权限
        Long userId = UserAppService.getCurrentUserId();
        User currentUser = userRepository.findById(userId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (!currentUser.isTeacher() && !currentUser.isAdmin()) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        // 2. 校验课程存在
        Course course = courseRepository.findById(command.getCourseId());
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }

        // 3. 校验成绩类型
        Score.validateType(command.getType());

        // 4. 批量录入
        int successCount = 0;
        int failCount = 0;

        for (BatchEnterScoreCommand.GradeItem item : command.getGrades()) {
            try {
                User student = userRepository.findByUsername(item.getStudentId());
                if (student == null || !student.isStudent()) {
                    failCount++;
                    continue;
                }

                // 检查是否已存在
                Score existing = scoreRepository.findByCourseIdAndStudentUserIdAndType(
                        command.getCourseId(), student.getId(), command.getType());
                if (existing != null) {
                    // 已存在则更新
                    existing.updateScore(item.getScore(), "批量录入覆盖");
                    scoreRepository.update(existing);
                } else {
                    Score score = Score.create(command.getCourseId(), student.getId(),
                            item.getScore(), command.getType(), null);
                    scoreRepository.save(score);
                }
                successCount++;
            } catch (Exception e) {
                log.warn("批量录入失败: studentId={}, error={}", item.getStudentId(), e.getMessage());
                failCount++;
            }
        }

        log.info("批量录入成绩完成: courseId={}, success={}, fail={}",
                command.getCourseId(), successCount, failCount);

        return BatchEnterScoreResponseDTO.builder()
                .successCount(successCount)
                .failCount(failCount)
                .build();
    }

    // ==================== 6.6 修改成绩（教师） ====================

    @Transactional(rollbackFor = Exception.class)
    public UpdateScoreResponseDTO updateScore(UpdateScoreCommand command) {
        // 1. 校验教师权限
        Long userId = UserAppService.getCurrentUserId();
        User currentUser = userRepository.findById(userId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (!currentUser.isTeacher() && !currentUser.isAdmin()) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        // 2. 校验课程存在
        Course course = courseRepository.findById(command.getCourseId());
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }

        // 3. 校验成绩类型
        Score.validateType(command.getType());

        // 4. 查找学生
        User student = userRepository.findByUsername(command.getStudentId());
        if (student == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 5. 查找原成绩
        Score existing = scoreRepository.findByCourseIdAndStudentUserIdAndType(
                command.getCourseId(), student.getId(), command.getType());
        if (existing == null) {
            throw new BusinessException(ResultCode.SCORE_NOT_FOUND);
        }

        // 6. 修改成绩
        int oldScore = existing.getScore();
        existing.updateScore(command.getScore(), command.getReason());
        scoreRepository.update(existing);

        log.info("成绩修改成功: courseId={}, studentId={}, oldScore={}, newScore={}",
                command.getCourseId(), command.getStudentId(), oldScore, command.getScore());

        return UpdateScoreResponseDTO.builder()
                .courseId(command.getCourseId())
                .studentId(command.getStudentId())
                .oldScore(oldScore)
                .newScore(command.getScore())
                .updateTime(existing.getUpdateTime() != null ? existing.getUpdateTime().format(FORMATTER) : null)
                .build();
    }

    // ==================== 6.7 获取课程成绩列表（教师/管理员） ====================

    public PageResult<CourseGradeDTO> getCourseGrades(Long courseId, CourseGradeListQuery query) {
        // 1. 校验课程存在
        Course course = courseRepository.findById(courseId);
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }

        // 2. 分页查成绩
        List<Score> scores = scoreRepository.findByCourseId(courseId, query.getType(),
                query.getOffset(), query.getPageSize());
        long total = scoreRepository.countByCourseId(courseId, query.getType());

        // 3. 构建DTO（关联学生信息）
        List<CourseGradeDTO> dtoList = new ArrayList<>();
        for (Score score : scores) {
            User student = userRepository.findById(score.getStudentUserId());
            UserProfile profile = userProfileRepository.findByUserId(score.getStudentUserId());

            dtoList.add(CourseGradeDTO.builder()
                    .studentId(profile != null ? profile.getStudentId() :
                            (student != null ? student.getUsername() : ""))
                    .realName(student != null ? student.getRealName() : "未知")
                    .className(profile != null ? profile.getClassName() : "")
                    .score(score.getScore())
                    .grade(score.getGrade())
                    .isPassed(score.isPassed())
                    .build());
        }

        return PageResult.of(total, dtoList, query.getPageNum(), query.getPageSize());
    }

    // ==================== 6.8 成绩统计（教师/管理员） ====================

    public CourseGradeStatisticsDTO getCourseGradeStatistics(Long courseId) {
        // 1. 校验课程存在
        Course course = courseRepository.findById(courseId);
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }

        // 2. 查该课程全部成绩
        List<Score> scores = scoreRepository.findByCourseId(courseId, null, 0, Integer.MAX_VALUE);
        long totalStudents = scoreRepository.countByCourseId(courseId, null);

        if (scores.isEmpty()) {
            return CourseGradeStatisticsDTO.builder()
                    .courseId(courseId)
                    .courseName(course.getName())
                    .courseCode(course.getCourseCode())
                    .totalStudents((int) totalStudents)
                    .avgScore(0.0)
                    .maxScore(0)
                    .minScore(0)
                    .passRate(0.0)
                    .distribution(new LinkedHashMap<>())
                    .build();
        }

        // 3. 统计
        double avgScore = scores.stream().mapToInt(Score::getScore).average().orElse(0.0);
        int maxScore = scores.stream().mapToInt(Score::getScore).max().orElse(0);
        int minScore = scores.stream().mapToInt(Score::getScore).min().orElse(0);
        long passCount = scores.stream().filter(Score::isPassed).count();
        double passRate = totalStudents > 0 ? (double) passCount / totalStudents * 100.0 : 0.0;

        // 4. 分布
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("优秀", 0);
        distribution.put("良好", 0);
        distribution.put("中等", 0);
        distribution.put("及格", 0);
        distribution.put("不及格", 0);
        for (Score score : scores) {
            distribution.merge(score.getGrade(), 1, Integer::sum);
        }

        return CourseGradeStatisticsDTO.builder()
                .courseId(courseId)
                .courseName(course.getName())
                .courseCode(course.getCourseCode())
                .totalStudents((int) totalStudents)
                .avgScore(Math.round(avgScore * 10.0) / 10.0)
                .maxScore(maxScore)
                .minScore(minScore)
                .passRate(Math.round(passRate * 10.0) / 10.0)
                .distribution(distribution)
                .build();
    }

    // ==================== 6.9 导出成绩（教师/管理员） ====================

    public byte[] exportCourseGrades(Long courseId) {
        // 1. 校验课程存在
        Course course = courseRepository.findById(courseId);
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }

        // 2. 查全部成绩
        List<Score> scores = scoreRepository.findByCourseId(courseId, null, 0, Integer.MAX_VALUE);

        // 3. 生成Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("成绩表");

            // 标题行
            Row titleRow = sheet.createRow(0);
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(course.getName() + " 成绩表");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5));

            // 表头
            Row headerRow = sheet.createRow(1);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            String[] headers = {"序号", "学号", "姓名", "班级", "成绩", "等级"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            int rowNum = 2;
            for (int i = 0; i < scores.size(); i++) {
                Score score = scores.get(i);
                Row row = sheet.createRow(rowNum++);
                User student = userRepository.findById(score.getStudentUserId());
                UserProfile profile = userProfileRepository.findByUserId(score.getStudentUserId());

                createCell(row, 0, String.valueOf(i + 1), dataStyle);
                createCell(row, 1, profile != null ? profile.getStudentId() :
                        (student != null ? student.getUsername() : ""), dataStyle);
                createCell(row, 2, student != null ? student.getRealName() : "未知", dataStyle);
                createCell(row, 3, profile != null ? profile.getClassName() : "", dataStyle);
                createCell(row, 4, String.valueOf(score.getScore()), dataStyle);
                createCell(row, 5, score.getGrade(), dataStyle);
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("成绩导出失败: courseId={}", courseId, e);
            throw new BusinessException(ResultCode.SCORE_EXPORT_FAILED);
        }
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }
}
