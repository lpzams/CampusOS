package com.campus.infrastructure.persistence.score;

import com.campus.domain.score.entity.Course;
import com.campus.domain.score.entity.Score;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScoreConverter {

    // ===== Score 转换 =====

    public Score toScore(ScorePO po) {
        if (po == null) return null;
        Score score = new Score();
        BeanUtils.copyProperties(po, score);
        return score;
    }

    public ScorePO toScorePO(Score score) {
        if (score == null) return null;
        ScorePO po = new ScorePO();
        BeanUtils.copyProperties(score, po);
        return po;
    }

    public List<Score> toScoreList(List<ScorePO> poList) {
        if (poList == null || poList.isEmpty()) {
            return new ArrayList<>();
        }
        return poList.stream().map(this::toScore).collect(Collectors.toList());
    }

    // ===== Course 转换 =====

    public Course toCourse(CoursePO po) {
        if (po == null) return null;
        Course course = new Course();
        BeanUtils.copyProperties(po, course);
        return course;
    }

    public CoursePO toCoursePO(Course course) {
        if (course == null) return null;
        CoursePO po = new CoursePO();
        BeanUtils.copyProperties(course, po);
        return po;
    }

    public List<Course> toCourseList(List<CoursePO> poList) {
        if (poList == null || poList.isEmpty()) {
            return new ArrayList<>();
        }
        return poList.stream().map(this::toCourse).collect(Collectors.toList());
    }
}
