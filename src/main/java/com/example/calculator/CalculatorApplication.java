package com.example.calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 科学计算器服务启动入口。
 *
 * <p>应用采用 DDD 分层结构，所有业务数据仅保存在当前 JVM 进程内。
 */
@SpringBootApplication
public class CalculatorApplication {

    /**
     * 启动 Spring Boot 应用。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(CalculatorApplication.class, args);
    }
}
