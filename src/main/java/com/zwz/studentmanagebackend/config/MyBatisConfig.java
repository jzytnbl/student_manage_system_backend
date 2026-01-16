package com.zwz.studentmanagebackend.config;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.zwz.studentmanagebackend.mapper")
public class MyBatisConfig {
}