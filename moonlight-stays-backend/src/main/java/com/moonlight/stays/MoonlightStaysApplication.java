package com.moonlight.stays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MoonlightStaysApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoonlightStaysApplication.class, args);
    }
}
