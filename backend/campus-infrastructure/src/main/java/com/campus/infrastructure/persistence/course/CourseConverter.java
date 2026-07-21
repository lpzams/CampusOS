package com.campus.infrastructure.persistence.course;

import com.campus.domain.course.entity.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CourseConverter {

    public Course toCourse(CoursePO po) { if (po == null) return null; Course c = new Course(); BeanUtils.copyProperties(po, c); return c; }
    public CoursePO toCoursePO(Course c) { if (c == null) return null; CoursePO po = new CoursePO(); BeanUtils.copyProperties(c, po); return po; }
    public List<Course> toCourseList(List<CoursePO> list) { if (list == null) return new ArrayList<>(); return list.stream().map(this::toCourse).collect(Collectors.toList()); }

    public Classroom toClassroom(ClassroomPO po) { if (po == null) return null; Classroom r = new Classroom(); BeanUtils.copyProperties(po, r); return r; }
    public ClassroomPO toClassroomPO(Classroom r) { if (r == null) return null; ClassroomPO po = new ClassroomPO(); BeanUtils.copyProperties(r, po); return po; }
    public List<Classroom> toClassroomList(List<ClassroomPO> list) { if (list == null) return new ArrayList<>(); return list.stream().map(this::toClassroom).collect(Collectors.toList()); }

    public Schedule toSchedule(SchedulePO po) { if (po == null) return null; Schedule s = new Schedule(); BeanUtils.copyProperties(po, s); return s; }
    public SchedulePO toSchedulePO(Schedule s) { if (s == null) return null; SchedulePO po = new SchedulePO(); BeanUtils.copyProperties(s, po); return po; }
    public List<Schedule> toScheduleList(List<SchedulePO> list) { if (list == null) return new ArrayList<>(); return list.stream().map(this::toSchedule).collect(Collectors.toList()); }

    public CourseEnrollment toEnrollment(CourseEnrollmentPO po) { if (po == null) return null; CourseEnrollment e = new CourseEnrollment(); BeanUtils.copyProperties(po, e); return e; }
    public CourseEnrollmentPO toEnrollmentPO(CourseEnrollment e) { if (e == null) return null; CourseEnrollmentPO po = new CourseEnrollmentPO(); BeanUtils.copyProperties(e, po); return po; }
    public List<CourseEnrollment> toEnrollmentList(List<CourseEnrollmentPO> list) { if (list == null) return new ArrayList<>(); return list.stream().map(this::toEnrollment).collect(Collectors.toList()); }
}
