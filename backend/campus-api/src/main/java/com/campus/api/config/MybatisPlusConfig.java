package com.campus.api.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置 —— DDD 洋葱架构·api 层（装配层）。
 *
 * <p>【职责】注册 MyBatis-Plus 的分页插件。注册后，任何 Mapper 用
 * {@code Page} 对象查询都会自动分页，后续功能直接用即可。
 *
 * <p>注：news 示例的仓储用的是手写 LIMIT，是为了演示"仓储接口可以自定义查询"。
 * 而这里的分页插件是给后续大多数功能准备的标准方式，二选一都行。
 *
 * <p>【新增功能时】不用改这里，直接享用分页能力。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // MySQL 分页
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
