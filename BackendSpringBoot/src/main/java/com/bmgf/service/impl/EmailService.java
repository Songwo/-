package com.bmgf.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your-email@example.com"); // 设置发件人
        message.setTo(to);
        message.setSubject("验证您的邮箱");
        message.setText("请点击以下链接验证您的邮箱：\n" +
                "http://your-frontend-url/verify-email?token=" + token);

        mailSender.send(message);
    }
}
