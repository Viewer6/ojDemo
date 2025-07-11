package com.viewer.friend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
@MapperScan("com.viewer.**.mapper")
public class OjFriendApplication {
    public static void main(String[] args) {
        SpringApplication.run(OjFriendApplication.class, args);
    }
}
