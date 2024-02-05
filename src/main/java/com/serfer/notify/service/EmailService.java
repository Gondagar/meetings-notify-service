package com.serfer.notify.service;

import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;


@Service
public class EmailService {

    private final Environment env;

    private MimeMessage message;

    private final JavaMailSender javaMailSender;

    public EmailService(Environment env, JavaMailSender javaMailSender) {
        this.env = env;
        this.javaMailSender = javaMailSender;
    }


    public void sendEmail(String message, String subject, String toEmail) {

        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailMessage.setFrom("notifymeeting@gmail.com");

        javaMailSender.send(mailMessage);
    }


}
