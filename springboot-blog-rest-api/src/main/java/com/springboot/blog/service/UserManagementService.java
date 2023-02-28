package com.springboot.blog.service;

import com.springboot.blog.dto.request.ForgotPasswordRequest;
import com.springboot.blog.dto.request.ResetPasswordRequest;
import com.springboot.blog.dto.response.MessageResponse;
import org.springframework.http.ResponseEntity;

public interface UserManagementService {

    ResponseEntity<MessageResponse> forgotPassword(ForgotPasswordRequest request);

    ResponseEntity<MessageResponse> resetPassword(ResetPasswordRequest request);
}
