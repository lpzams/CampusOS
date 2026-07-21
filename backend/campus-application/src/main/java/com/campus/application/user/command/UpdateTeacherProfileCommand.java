package com.campus.application.user.command;

import lombok.Data;

/**
 * 修改教师详细信息命令（2.9）
 */
@Data
public class UpdateTeacherProfileCommand {

    /** 职称 */
    private String title;

    /** 研究方向 */
    private String researchDirection;

    /** 办公室 */
    private String office;

    /** 个人简介 */
    private String introduction;
}
