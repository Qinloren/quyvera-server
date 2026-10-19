package com.zeeyeh.quyvera.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.zeeyeh.quyvera.common",
        "com.zeeyeh.quyvera.user"
})
@MapperScan(basePackages = "com.zeeyeh.quyvera.user.mapper")
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
