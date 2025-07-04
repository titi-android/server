package com.example.busnotice.util;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmailSender {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    @Value("${email.de}")
    private String de;
    @Value("${email.back}")
    private String back;
    @Value("${email.aos}")
    private String aos;

    /**
     * 이메일 전송
     */
    public void sendMailNotice(String username, String title, String content) {
        ArrayList<String> emails = new ArrayList<>();
        emails.add(de);
        emails.add(back);
        emails.add(aos);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false,
                    "UTF-8");
            mimeMessageHelper.setTo(emails.toArray(new String[0])); // 수신자 메일
            mimeMessageHelper.setSubject(title); // 메일 제목
            mimeMessageHelper.setText(setContext(todayDate(), username, title, content),
                    true); // 메일 본문
            javaMailSender.send(mimeMessage);

            log.info("SUCCEEDED TO SEND EMAIL to {}", emails.toArray(new String[0]));
        } catch (Exception e) {
            log.error("FAILED TO SEND EMAIL to {}", emails.toArray(new String[0]), e);
            throw new RuntimeException(e);
        }
    }

    public String todayDate() {
        ZonedDateTime todayDate = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                .atZone(ZoneId.of("Asia/Seoul"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        return todayDate.format(formatter);
    }

    // Thymeleaf 통한 HTML 적용 (date, title, content, url 변수를 템플릿에 전달)
    public String setContext(String date, String username, String title, String content) {
        Context context = new Context();
        context.setVariable("date", date);
        context.setVariable("username", username);
        context.setVariable("title", title);
        context.setVariable("content", content);
        return templateEngine.process("email", context);
    }
}