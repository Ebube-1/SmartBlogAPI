package com.springboot.blog.service;

import com.springboot.blog.payload.SignupDto;
import org.springframework.http.ResponseEntity;

public interface SignupService {
    ResponseEntity<String> registerUser (SignupDto signupDto);
}
