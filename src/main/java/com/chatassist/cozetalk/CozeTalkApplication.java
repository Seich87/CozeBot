package com.chatassist.cozetalk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CozeTalkApplication {
    public static void main(String[] args) {
        SpringApplication.run(CozeTalkApplication.class, args);
    }
}