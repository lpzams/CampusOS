package com.campus.domain.course.entity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 教室类型枚举
 */
public final class ClassroomType {

    private ClassroomType() {}

    public static final int NORMAL      = 1; // 普通教室
    public static final int MULTIMEDIA  = 2; // 多媒体教室
    public static final int SMART       = 3; // 智慧教室
    public static final int LAB         = 4; // 实验室
    public static final int COMPUTER    = 5; // 机房

    private static final Map<Integer, String> NAMES = new LinkedHashMap<>();
    static {
        NAMES.put(NORMAL,     "普通教室");
        NAMES.put(MULTIMEDIA, "多媒体教室");
        NAMES.put(SMART,      "智慧教室");
        NAMES.put(LAB,        "实验室");
        NAMES.put(COMPUTER,   "机房");
    }

    public static String getName(int type) {
        return NAMES.getOrDefault(type, "未知");
    }

    public static boolean isValid(int type) {
        return NAMES.containsKey(type);
    }
}
