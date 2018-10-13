package com.atguigu.gmall.manager;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.zookeeper.ZooKeeper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.CountDownLatch;

//扫描所有mybatis的mapper文件
@EnableDubbo//开启dubbo
@MapperScan("com.atguigu.gmall.manager.mapper")
@SpringBootApplication
@EnableTransactionManagement
public class GmallManagerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallManagerServiceApplication.class, args);



	}
}
