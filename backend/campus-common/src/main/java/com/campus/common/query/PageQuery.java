package com.campus.common.query;

import lombok.Data;

/**
 * 分页查询基类
 * <p>
 * 所有分页查询的 Command/Query 都继承这个类
 */
@Data
public class PageQuery {

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}