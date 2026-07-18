package com.campus.common.api;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 统一分页返回结构
 */
@Data
public class PageResult<T> implements Serializable {

    private Long total;
    private List<T> list;
    private Integer pageNum;
    private Integer pageSize;
    private Integer totalPages;

    public static <T> PageResult<T> of(Long total, List<T> list, Integer pageNum, Integer pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setList(list);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotalPages((int) Math.ceil((double) total / pageSize));
        return result;
    }

    public static <T> PageResult<T> of(Long total, List<T> list) {
        return of(total, list, 1, 10);
    }

    public static <T> PageResult<T> empty() {
        return of(0L, List.of());
    }
}