package com.campus.infrastructure.persistence.news;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 新闻 Mapper —— MyBatis-Plus 的数据访问接口。
 *
 * <p>继承 {@link BaseMapper} 就自带了单表 CRUD（insert/selectById/updateById/delete...），
 * 简单查询不用写一行 SQL。复杂 SQL 才需要在这里加自定义方法 + 对应的 XML/注解。
 *
 * <p>【新增功能时】照抄本类：{@code interface XxxMapper extends BaseMapper<XxxPO>}。
 */
@Mapper
public interface NewsMapper extends BaseMapper<NewsPO> {
}
