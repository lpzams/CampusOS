package com.campus.application.user.command;

import lombok.Data;

/**
 * 修改学生详细信息命令（2.8）
 */
@Data
public class UpdateStudentProfileCommand {

    /** 专业 */
    private String major;

    /** 班级 */
    private String className;

    /** 入学年份 */
    private String enrollmentYear;

    /** 宿舍号 */
    private String dormitory;

    /** 辅导员/导师 */
    private String advisor;
}
