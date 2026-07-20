package com.campus.application.exam.service;

import com.campus.application.exam.command.*;
import com.campus.application.exam.dto.*;
import com.campus.application.user.service.UserAppService;
import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import com.campus.domain.exam.entity.*;
import com.campus.domain.exam.repository.*;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.entity.UserProfile;
import com.campus.domain.user.repository.UserProfileRepository;
import com.campus.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamAdminService {

    private final ExamRepository examRepository;
    private final ExamRoomRepository examRoomRepository;
    private final ExamSeatRepository examSeatRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== 7.3 创建考试安排 ====================

    @Transactional(rollbackFor = Exception.class)
    public CreateExamResponseDTO createExam(CreateExamCommand command) {
        getAdminUser();

        LocalDate examDate = parseDate(command.getExamDate());
        Exam.validateType(command.getExamType());

        // 检查教室冲突
        if (examRepository.existsByDateAndClassroom(examDate, command.getClassroom(), null)) {
            throw new BusinessException("该教室在" + command.getExamDate() + "已被占用");
        }

        Exam exam = Exam.create(
                command.getCourseId(), command.getCourseName(), command.getCourseCode(),
                examDate, command.getExamTime(), command.getBuilding(),
                command.getClassroom(), command.getExamType()
        );
        examRepository.save(exam);

        log.info("考试安排创建成功: id={}, courseName={}, examDate={}", exam.getId(), exam.getCourseName(), exam.getExamDate());

        return CreateExamResponseDTO.builder()
                .id(exam.getId())
                .courseName(exam.getCourseName())
                .examDate(exam.getExamDate().format(DATE_FMT))
                .examTime(exam.getExamTime())
                .classroom(exam.getClassroom())
                .status(exam.getStatusDesc())
                .statusCode(exam.getStatus())
                .createTime(exam.getCreateTime() != null ? exam.getCreateTime().format(FORMATTER) : null)
                .build();
    }

    // ==================== 7.4 更新考试安排 ====================

    @Transactional(rollbackFor = Exception.class)
    public UpdateExamResponseDTO updateExam(Long id, UpdateExamCommand command) {
        getAdminUser();

        Exam exam = examRepository.findById(id);
        if (exam == null) {
            throw new BusinessException(ResultCode.EXAM_NOT_FOUND);
        }

        LocalDate examDate = command.getExamDate() != null ? parseDate(command.getExamDate()) : null;
        String classroom = command.getClassroom() != null ? command.getClassroom() : exam.getClassroom();

        // 检查教室冲突
        if (examDate != null || command.getClassroom() != null) {
            LocalDate checkDate = examDate != null ? examDate : exam.getExamDate();
            if (examRepository.existsByDateAndClassroom(checkDate, classroom, id)) {
                throw new BusinessException("该教室已被占用");
            }
        }

        exam.updateFields(examDate, command.getExamTime(), classroom, command.getBuilding());
        examRepository.update(exam);

        log.info("考试安排更新成功: id={}", id);

        return UpdateExamResponseDTO.builder()
                .id(exam.getId())
                .courseName(exam.getCourseName())
                .examDate(exam.getExamDate() != null ? exam.getExamDate().format(DATE_FMT) : null)
                .examTime(exam.getExamTime())
                .classroom(exam.getClassroom())
                .updateTime(exam.getUpdateTime() != null ? exam.getUpdateTime().format(FORMATTER) : null)
                .build();
    }

    // ==================== 7.5 删除考试安排 ====================

    @Transactional(rollbackFor = Exception.class)
    public void deleteExam(Long id) {
        getAdminUser();

        Exam exam = examRepository.findById(id);
        if (exam == null) {
            throw new BusinessException(ResultCode.EXAM_NOT_FOUND);
        }

        examRoomRepository.deleteByExamId(id);
        examSeatRepository.deleteByExamId(id);
        examRepository.delete(id);

        log.info("考试安排删除成功: id={}", id);
    }

    // ==================== 7.6 批量创建考试安排 ====================

    @Transactional(rollbackFor = Exception.class)
    public BatchCreateExamResponseDTO batchCreateExam(BatchCreateExamCommand command) {
        getAdminUser();

        int successCount = 0;
        int failCount = 0;
        List<Long> ids = new ArrayList<>();

        for (CreateExamCommand item : command.getExams()) {
            try {
                CreateExamResponseDTO result = createExam(item);
                ids.add(result.getId());
                successCount++;
            } catch (Exception e) {
                log.warn("批量创建考试失败: courseName={}, error={}", item.getCourseName(), e.getMessage());
                failCount++;
            }
        }

        log.info("批量创建考试完成: success={}, fail={}", successCount, failCount);

        return BatchCreateExamResponseDTO.builder()
                .successCount(successCount)
                .failCount(failCount)
                .ids(ids)
                .build();
    }

    // ==================== 7.7 获取考场安排 ====================

    public ExamRoomDTO getExamRooms(Long examId) {
        Exam exam = examRepository.findById(examId);
        if (exam == null) {
            throw new BusinessException(ResultCode.EXAM_NOT_FOUND);
        }

        List<ExamRoom> rooms = examRoomRepository.findByExamId(examId);
        List<ExamSeat> allSeats = examSeatRepository.findByExamId(examId);

        // 按 roomId 分组座位
        Map<Long, List<ExamSeat>> seatMap = new HashMap<>();
        for (ExamSeat seat : allSeats) {
            seatMap.computeIfAbsent(seat.getRoomId(), k -> new ArrayList<>()).add(seat);
        }

        List<ExamRoomDTO.RoomInfo> roomInfos = new ArrayList<>();
        for (ExamRoom room : rooms) {
            List<ExamSeat> roomSeats = seatMap.getOrDefault(room.getId(), Collections.emptyList());
            List<ExamRoomDTO.SeatInfo> seatInfos = new ArrayList<>();

            for (ExamSeat seat : roomSeats) {
                User student = userRepository.findById(seat.getStudentUserId());
                UserProfile profile = userProfileRepository.findByUserId(seat.getStudentUserId());

                seatInfos.add(ExamRoomDTO.SeatInfo.builder()
                        .seatNumber(seat.getSeatNumber())
                        .studentId(profile != null ? profile.getStudentId() :
                                (student != null ? student.getUsername() : ""))
                        .realName(student != null ? student.getRealName() : "未知")
                        .build());
            }

            roomInfos.add(ExamRoomDTO.RoomInfo.builder()
                    .roomId(room.getId())
                    .building(room.getBuilding())
                    .classroom(room.getClassroom())
                    .capacity(room.getCapacity())
                    .studentCount(roomSeats.size())
                    .seats(seatInfos)
                    .build());
        }

        return ExamRoomDTO.builder()
                .examId(exam.getId())
                .courseName(exam.getCourseName())
                .examDate(exam.getExamDate() != null ? exam.getExamDate().format(DATE_FMT) : null)
                .examTime(exam.getExamTime())
                .rooms(roomInfos)
                .build();
    }

    // ==================== 7.8 分配考场座位 ====================

    @Transactional(rollbackFor = Exception.class)
    public SeatAssignResponseDTO assignSeats(SeatAssignCommand command) {
        getAdminUser();

        Exam exam = examRepository.findById(command.getExamId());
        if (exam == null) {
            throw new BusinessException(ResultCode.EXAM_NOT_FOUND);
        }

        // 查已有座位号
        List<ExamSeat> existingSeats = examSeatRepository.findByRoomId(command.getRoomId());
        int nextSeatNum = existingSeats.stream()
                .mapToInt(ExamSeat::getSeatNumber)
                .max().orElse(0);

        List<ExamSeat> newSeats = new ArrayList<>();
        for (String studentId : command.getStudentIds()) {
            User student = userRepository.findByUsername(studentId);
            if (student == null || !student.isStudent()) {
                log.warn("分配座位跳过: studentId={}, 用户不存在或不是学生", studentId);
                continue;
            }
            nextSeatNum++;
            newSeats.add(ExamSeat.create(command.getExamId(), command.getRoomId(), nextSeatNum, student.getId()));
        }

        examSeatRepository.saveBatch(newSeats);

        log.info("座位分配成功: examId={}, roomId={}, count={}", command.getExamId(), command.getRoomId(), newSeats.size());

        return SeatAssignResponseDTO.builder()
                .examId(command.getExamId())
                .roomId(command.getRoomId())
                .assignedCount(newSeats.size())
                .assignTime(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    // ==================== 私有方法 ====================

    private User getAdminUser() {
        Long userId = UserAppService.getCurrentUserId();
        User user = userRepository.findById(userId);
        if (user == null || !user.isAdmin()) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }
        return user;
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FMT);
        } catch (Exception e) {
            throw new BusinessException("日期格式不正确，正确格式：yyyy-MM-dd");
        }
    }
}
