package com.campus.infrastructure.persistence.exam;

import com.campus.domain.exam.entity.Exam;
import com.campus.domain.exam.entity.ExamReminder;
import com.campus.domain.exam.entity.ExamRoom;
import com.campus.domain.exam.entity.ExamSeat;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExamConverter {

    // ===== Exam =====
    public Exam toExam(ExamPO po) {
        if (po == null) return null;
        Exam exam = new Exam();
        BeanUtils.copyProperties(po, exam);
        return exam;
    }

    public ExamPO toExamPO(Exam exam) {
        if (exam == null) return null;
        ExamPO po = new ExamPO();
        BeanUtils.copyProperties(exam, po);
        return po;
    }

    public List<Exam> toExamList(List<ExamPO> poList) {
        if (poList == null || poList.isEmpty()) return new ArrayList<>();
        return poList.stream().map(this::toExam).collect(Collectors.toList());
    }

    // ===== ExamRoom =====
    public ExamRoom toExamRoom(ExamRoomPO po) {
        if (po == null) return null;
        ExamRoom room = new ExamRoom();
        BeanUtils.copyProperties(po, room);
        return room;
    }

    public ExamRoomPO toExamRoomPO(ExamRoom room) {
        if (room == null) return null;
        ExamRoomPO po = new ExamRoomPO();
        BeanUtils.copyProperties(room, po);
        return po;
    }

    public List<ExamRoom> toExamRoomList(List<ExamRoomPO> poList) {
        if (poList == null || poList.isEmpty()) return new ArrayList<>();
        return poList.stream().map(this::toExamRoom).collect(Collectors.toList());
    }

    // ===== ExamSeat =====
    public ExamSeat toExamSeat(ExamSeatPO po) {
        if (po == null) return null;
        ExamSeat seat = new ExamSeat();
        BeanUtils.copyProperties(po, seat);
        return seat;
    }

    public ExamSeatPO toExamSeatPO(ExamSeat seat) {
        if (seat == null) return null;
        ExamSeatPO po = new ExamSeatPO();
        BeanUtils.copyProperties(seat, po);
        return po;
    }

    public List<ExamSeat> toExamSeatList(List<ExamSeatPO> poList) {
        if (poList == null || poList.isEmpty()) return new ArrayList<>();
        return poList.stream().map(this::toExamSeat).collect(Collectors.toList());
    }

    // ===== ExamReminder =====
    public ExamReminder toExamReminder(ExamReminderPO po) {
        if (po == null) return null;
        ExamReminder reminder = new ExamReminder();
        BeanUtils.copyProperties(po, reminder);
        // 转换 enabled: Integer -> Boolean
        if (po.getEnabled() != null) {
            reminder.setEnabled(po.getEnabled() == 1);
        }
        return reminder;
    }

    public ExamReminderPO toExamReminderPO(ExamReminder reminder) {
        if (reminder == null) return null;
        ExamReminderPO po = new ExamReminderPO();
        BeanUtils.copyProperties(reminder, po);
        // 转换 enabled: Boolean -> Integer
        if (reminder.getEnabled() != null) {
            po.setEnabled(reminder.getEnabled() ? 1 : 0);
        }
        return po;
    }
}
