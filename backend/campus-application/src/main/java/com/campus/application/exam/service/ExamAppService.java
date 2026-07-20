package com.campus.application.exam.service;

import com.campus.application.exam.command.ExamReminderCommand;
import com.campus.application.exam.dto.*;
import com.campus.application.user.service.UserAppService;
import com.campus.domain.exam.entity.*;
import com.campus.domain.exam.repository.*;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.entity.UserProfile;
import com.campus.domain.user.repository.UserProfileRepository;
import com.campus.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamAppService {

    private final ExamRepository examRepository;
    private final ExamSeatRepository examSeatRepository;
    private final ExamReminderRepository examReminderRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== 7.1 获取考试安排 ====================

    public List<ExamDTO> getExamList(String status) {
        List<Exam> exams = examRepository.findAll(status);
        return buildExamDTOs(exams);
    }

    // ==================== 7.2 获取考试日历 ====================

    public ExamCalendarDTO getExamCalendar(int year, int month) {
        List<Exam> exams = examRepository.findByMonth(year, month);

        // 按日期分组
        Map<LocalDate, List<Exam>> dateMap = new LinkedHashMap<>();
        for (Exam exam : exams) {
            dateMap.computeIfAbsent(exam.getExamDate(), k -> new ArrayList<>()).add(exam);
        }

        List<ExamCalendarDTO.CalendarEvent> events = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Exam>> entry : dateMap.entrySet()) {
            List<ExamCalendarDTO.DayExam> dayExams = new ArrayList<>();
            for (Exam exam : entry.getValue()) {
                dayExams.add(ExamCalendarDTO.DayExam.builder()
                        .id(exam.getId())
                        .courseName(exam.getCourseName())
                        .time(exam.getExamTime())
                        .classroom(exam.getClassroom())
                        .status(exam.getStatus())
                        .build());
            }
            events.add(ExamCalendarDTO.CalendarEvent.builder()
                    .date(entry.getKey().format(DATE_FMT))
                    .dayOfWeek(entry.getKey().getDayOfWeek().getValue())
                    .exams(dayExams)
                    .build());
        }

        return ExamCalendarDTO.builder()
                .year(year)
                .month(month)
                .events(events)
                .build();
    }

    // ==================== 7.9 我的考试安排 ====================

    public ExamMyDTO getMyExams(String status) {
        Long userId = UserAppService.getCurrentUserId();
        List<Exam> exams = examRepository.findByStudentUserId(userId, status);
        List<ExamSeat> seats = examSeatRepository.findByStudentUserId(userId);

        // 构建 examId -> seatNumber 映射
        Map<Long, String> seatMap = new HashMap<>();
        for (ExamSeat seat : seats) {
            seatMap.put(seat.getExamId(), String.valueOf(seat.getSeatNumber()));
        }

        int pending = 0, completed = 0;
        List<ExamMyDTO.ExamItem> items = new ArrayList<>();
        for (Exam exam : exams) {
            if (exam.isPending()) {
                pending++;
            } else {
                completed++;
            }

            // 计算倒计时
            String countdown = null;
            if (exam.isPending() && exam.getExamDate() != null) {
                long days = ChronoUnit.DAYS.between(LocalDate.now(), exam.getExamDate());
                long hours = ChronoUnit.HOURS.between(java.time.LocalDateTime.now(),
                        exam.getExamDate().atTime(java.time.LocalTime.parse(
                                exam.getExamTime().split("-")[0])));
                if (days > 0) {
                    countdown = days + "天" + (hours % 24) + "小时";
                } else {
                    countdown = hours + "小时";
                }
            }

            items.add(ExamMyDTO.ExamItem.builder()
                    .id(exam.getId())
                    .courseName(exam.getCourseName())
                    .courseCode(exam.getCourseCode())
                    .examDate(exam.getExamDate() != null ? exam.getExamDate().format(DATE_FMT) : null)
                    .examTime(exam.getExamTime())
                    .building(exam.getBuilding())
                    .classroom(exam.getClassroom())
                    .seatNumber(seatMap.get(exam.getId()))
                    .status(exam.getStatusDesc())
                    .statusCode(exam.getStatus())
                    .countdown(countdown)
                    .build());
        }

        return ExamMyDTO.builder()
                .total(exams.size())
                .pending(pending)
                .completed(completed)
                .list(items)
                .build();
    }

    // ==================== 7.10 考试提醒设置 ====================

    public ExamReminderDTO getReminder() {
        Long userId = UserAppService.getCurrentUserId();
        ExamReminder reminder = examReminderRepository.findByUserId(userId);
        if (reminder == null) {
            return ExamReminderDTO.builder().enabled(true).reminderMinutes(60).build();
        }
        return ExamReminderDTO.builder()
                .enabled(reminder.getEnabled())
                .reminderMinutes(reminder.getReminderMinutes())
                .build();
    }

    public ExamReminderDTO updateReminder(ExamReminderCommand command) {
        Long userId = UserAppService.getCurrentUserId();
        ExamReminder reminder = examReminderRepository.findByUserId(userId);
        if (reminder == null) {
            reminder = ExamReminder.create(userId);
        }
        reminder.updateSettings(command.getEnabled(), command.getReminderMinutes());
        examReminderRepository.saveOrUpdate(reminder);

        return ExamReminderDTO.builder()
                .enabled(reminder.getEnabled())
                .reminderMinutes(reminder.getReminderMinutes())
                .build();
    }

    // ==================== 私有方法 ====================

    private List<ExamDTO> buildExamDTOs(List<Exam> exams) {
        List<ExamDTO> dtos = new ArrayList<>();
        for (Exam exam : exams) {
            dtos.add(ExamDTO.builder()
                    .id(exam.getId())
                    .courseName(exam.getCourseName())
                    .courseCode(exam.getCourseCode())
                    .examDate(exam.getExamDate() != null ? exam.getExamDate().format(DATE_FMT) : null)
                    .examTime(exam.getExamTime())
                    .building(exam.getBuilding())
                    .classroom(exam.getClassroom())
                    .status(exam.getStatusDesc())
                    .statusCode(exam.getStatus())
                    .build());
        }
        return dtos;
    }
}
