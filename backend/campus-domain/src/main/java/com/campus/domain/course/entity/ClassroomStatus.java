package com.campus.domain.course.entity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 教室行政状态
 */
public final class ClassroomStatus {

    private ClassroomStatus() {}

    public static final int FREE      = 0; // 空闲
    public static final int OCUUPY = 1; // 占用
    public static final int DISABLED    = 2; // 停用

    private static final Map<Integer, String> NAMES = new LinkedHashMap<>();
    static {
        NAMES.put(FREE,        "空闲");
        NAMES.put(OCUUPY,      "占用");
        NAMES.put(DISABLED,    "停用");
    }

    public static String getName(int status) {
        return NAMES.getOrDefault(status, "未知");
    }
}
