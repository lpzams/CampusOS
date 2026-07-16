package com.campus.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园门户系统 —— 后端启动类。
 *
 * <p>【DDD 洋葱架构·api 层（最外层）】
 * <ul>
 *   <li>这里是整个后端唯一的启动入口，也是 Spring 扫描的根。</li>
 *   <li>{@code @SpringBootApplication} 默认扫描 com.campus 下所有 @Component/@Service/@Repository，
 *       所以 application / infrastructure 层的 Spring Bean 都能被扫到。</li>
 *   <li>{@code @MapperScan} 单独指定 MyBatis-Plus 的 Mapper 接口所在包。</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = "com.campus")
@MapperScan("com.campus.infrastructure.persistence")
public class CampusApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusApplication.class, args);
    }
}
