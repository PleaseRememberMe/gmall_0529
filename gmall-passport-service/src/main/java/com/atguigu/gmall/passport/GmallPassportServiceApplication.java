package com.atguigu.gmall.passport;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo
//指定mybatis mapper 的扫描路径
@MapperScan("com.atguigu.gmall.passport.mapper")
@SpringBootApplication
public class GmallPassportServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallPassportServiceApplication.class, args);
	}
}
