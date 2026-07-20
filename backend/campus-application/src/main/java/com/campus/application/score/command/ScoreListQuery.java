package com.campus.application.score.command;

import lombok.Data;

/**
 * 成绩列表查询参数（6.1）
 */
@Data
public class ScoreListQuery {

    /** 学期（可选），如：2024-2025-1 */
    private String semester;

    /** 类型（可选）：EXAM-考试 USUAL-平时 */
    private String type;
}
