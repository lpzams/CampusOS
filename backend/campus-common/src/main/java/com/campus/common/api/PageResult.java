package com.campus.common.api;

import java.util.Collections;
import java.util.List;

/**
 * 统一分页返回结构。
 *
 * <p>所有分页查询接口都返回这个结构，前端拿到的字段名固定，
 * 后端换分页插件也不影响接口契约。
 *
 * @param <T> 列表元素类型（通常是某个 DTO）
 */
public class PageResult<T> {

    /** 当前页码，从 1 开始 */
    private long pageNum;
    /** 每页条数 */
    private long pageSize;
    /** 总记录数 */
    private long total;
    /** 总页数 */
    private long pages;
    /** 当前页数据 */
    private List<T> list;

    public PageResult() {
        this.list = Collections.emptyList();
    }

    public PageResult(long pageNum, long pageSize, long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = pageSize == 0 ? 0 : (total + pageSize - 1) / pageSize;
        this.list = list == null ? Collections.emptyList() : list;
    }

    /** 快捷构造：空页 */
    public static <T> PageResult<T> empty(long pageNum, long pageSize) {
        return new PageResult<>(pageNum, pageSize, 0, Collections.emptyList());
    }

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
