package com.campus.common.query;

/**
 * 分页查询参数基类。
 *
 * <p>application 层所有"分页查询对象"都继承它，只需再补充自己的筛选字段。
 * 例如 {@code NewsPageQuery extends PageQuery} 里只写 keyword、category 即可。
 */
public class PageQuery {

    /** 当前页码，从 1 开始 */
    private long pageNum = 1;

    /** 每页条数 */
    private long pageSize = 10;

    public long getPageNum() {
        return pageNum <= 0 ? 1 : pageNum;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum;
    }

    public long getPageSize() {
        // 兜底：防止前端传超大 pageSize 拖垮数据库
        if (pageSize <= 0) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
}
