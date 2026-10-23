package com.zeeyeh.quyvera.auth;

import com.zeeyeh.quyvera.user.api.v1.UserServiceGrpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Qinloren
 * @date 2026/10/23 16:56
 */
@SpringBootApplication(scanBasePackages = {
        "com.zeeyeh.quyvera.common",
        "com.zeeyeh.quyvera.auth"
})
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
