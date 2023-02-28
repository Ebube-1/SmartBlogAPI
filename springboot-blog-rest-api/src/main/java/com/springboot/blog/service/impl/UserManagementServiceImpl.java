package com.springboot.blog.service.impl;

import com.springboot.blog.dto.EmailDto;
import com.springboot.blog.dto.request.ForgotPasswordRequest;
import com.springboot.blog.dto.request.ResetPasswordRequest;
import com.springboot.blog.dto.response.MessageResponse;
import com.springboot.blog.entity.ForgotPasswordToken;
import com.springboot.blog.entity.User;
import com.springboot.blog.exception.ClientRequestException;
import com.springboot.blog.repository.ForgotPasswordTokenRepository;
import com.springboot.blog.repository.UserRepository;
import com.springboot.blog.service.EmailService;
import com.springboot.blog.service.RandomStringGenerator;
import com.springboot.blog.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final RandomStringGenerator randomStringGenerator;
    private final PasswordEncoder passwordEncoder;
    private final ForgotPasswordTokenRepository forgotPasswordTokenRepository;
    private final EmailService emailService;

    @Value("${app.user-management.token-expiry-in-minutes}")
    private long expiryInMinutes;

    @Override
    public ResponseEntity<MessageResponse> forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new ClientRequestException("Email not found"));
        String token = randomStringGenerator.randomText(6);
        String hashedToken = passwordEncoder.encode(token);
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(expiryInMinutes);

        //delete any previous hash if existing
        forgotPasswordTokenRepository.deleteByUserId(user.getId());

        //save new hash
        ForgotPasswordToken forgotPasswordToken = ForgotPasswordToken.builder()
                .user(user)
                .tokenHash(hashedToken)
                .expiryTime(expiryTime)
                .used(false)
                .build();
        forgotPasswordTokenRepository.save(forgotPasswordToken);

        //send email with token and time it would expire
        EmailDto emailDto = EmailDto.builder()
                .recipientEmail(user.getEmail())
                .subject("Reset Email Token")
                .message(String.format("Your reset token is %s. It will expire in %s minutes.", token, expiryInMinutes))
                .build();

        emailService.sendEmail(emailDto);

        String message = String.format(
                "A token has been sent to your email: %s. It will expire in %s minutes.", user.getEmail(), expiryInMinutes
        );
        return new ResponseEntity<>(new MessageResponse(message), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<MessageResponse> resetPassword(ResetPasswordRequest request) {
        // new password and password confirmation must match
        if(!request.getNewPassword().equals(request.getConfirmPassword())){
            throw new ClientRequestException("Passwords do not match");
        }
        //if user email does not exist in the repo
        User user = userRepository.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new ClientRequestException("Email not found"));

        ForgotPasswordToken forgotPasswordToken = forgotPasswordTokenRepository.findByUser(user)
                .orElseThrow(() -> new ClientRequestException("Token does not exist for this user"));

        if(forgotPasswordToken.isUsed()){
            throw new ClientRequestException("Token has already been used");
        }

        //if token customer inputs and generated token in DB does not match
        if(!passwordEncoder.matches(request.getToken(), forgotPasswordToken.getTokenHash())){
            throw new ClientRequestException("Invalid token submitted");
        }
        if(LocalDateTime.now().isAfter(forgotPasswordToken.getExpiryTime())){
            throw new ClientRequestException("Submitted token has expired");
        }

        String hashedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(hashedPassword);
        userRepository.save(user);

        forgotPasswordToken.setUsed(true);
        forgotPasswordTokenRepository.save(forgotPasswordToken);

        String message = "Your password has been reset successfully";
        return new ResponseEntity<>(new MessageResponse(message), HttpStatus.OK);
    }
}
