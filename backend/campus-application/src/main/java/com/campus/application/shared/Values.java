package com.campus.application.shared;

import com.campus.common.exception.BusinessException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Values {
    private Values() {}

    public static String required(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null || value.toString().isBlank()) throw new BusinessException(400, key + " 不能为空");
        return value.toString();
    }

    public static BigDecimal decimal(Object value) {
        try { return new BigDecimal(value.toString()); }
        catch (Exception e) { throw new BusinessException(400, "数字格式错误"); }
    }

    public static long longValue(Object value) {
        try { return value instanceof Number number ? number.longValue() : Long.parseLong(value.toString()); }
        catch (Exception e) { throw new BusinessException(400, "整数格式错误"); }
    }

    public static int intValue(Object value) { return Math.toIntExact(longValue(value)); }

    public static Map<String, Object> owned(Long userId, Map<String, Object> data) {
        Map<String, Object> copy = new LinkedHashMap<>(data);
        copy.put("userId", userId);
        return copy;
    }

    public static BusinessException invalid(RuntimeException e) {
        return new BusinessException(400, e.getMessage());
    }
}
