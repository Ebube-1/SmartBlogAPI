package com.springboot.blog.service.impl;

import com.springboot.blog.dto.EmailDto;
import com.springboot.blog.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.email.service-type", havingValue = "mock")
public class MockEmailServiceImpl implements EmailService {

    @Override
    public void sendEmail(EmailDto emailDto) {
        log.info("email: " + emailDto);
    }
}
