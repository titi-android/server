package com.example.busnotice.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.threeten.bp.LocalDateTime;

@RestController
@Slf4j
public class test {

    //    @GetMapping("/api/v1/test")
    public void showCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("now = " + now);
        log.info("now: {}", now.toString());
    }
}
