package com.classroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI智慧教研室 - 主程序入口
 */
@SpringBootApplication(exclude = {
        org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration.class
})
public class ClassroomApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClassroomApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  AI智慧教研室 启动成功!");
        System.out.println("  http://localhost:8080");
        System.out.println("========================================\n");
    }
}
