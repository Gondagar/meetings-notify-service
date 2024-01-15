package com.serfer.notify.controller;

import com.serfer.notify.model.Email;
import com.serfer.notify.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping(value = "/sendmail")
    public ResponseEntity sendmail(@RequestBody Email email) {

        log.info("Info to sent {}",  email);
        emailService.sendEmail(email.getMessage(), email.getSubject(), email.getEmailTo());

        return new ResponseEntity(HttpStatus.OK);
    }
}