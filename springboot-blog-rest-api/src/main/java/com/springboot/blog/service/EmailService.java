package com.springboot.blog.service;

import com.springboot.blog.dto.EmailDto;

public interface EmailService {
    void sendEmail(EmailDto emailDto);
}
