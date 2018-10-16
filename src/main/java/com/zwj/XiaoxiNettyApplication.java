package com.zwj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
//扫描mybatis mapper包路径
@MapperScan(basePackages = "com.zwj.mapper")
public class XiaoxiNettyApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaoxiNettyApplication.class, args);
    }
}
