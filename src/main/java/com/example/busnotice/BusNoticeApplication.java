package com.example.busnotice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class BusNoticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusNoticeApplication.class, args);
    }

}
