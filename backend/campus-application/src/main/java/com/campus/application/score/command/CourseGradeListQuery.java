package com.campus.application.score.command;

import com.campus.common.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程成绩列表查询参数（6.7）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseGradeListQuery extends PageQuery {

    /** 类型（可选）：EXAM-考试 USUAL-平时 */
    private String type;
}
