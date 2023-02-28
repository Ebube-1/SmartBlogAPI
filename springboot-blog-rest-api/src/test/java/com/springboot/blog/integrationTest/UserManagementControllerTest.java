package com.springboot.blog.integrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.springboot.blog.dto.EmailDto;
import com.springboot.blog.dto.request.ForgotPasswordRequest;
import com.springboot.blog.dto.request.ResetPasswordRequest;
import com.springboot.blog.entity.ForgotPasswordToken;
import com.springboot.blog.entity.User;
import com.springboot.blog.repository.ForgotPasswordTokenRepository;
import com.springboot.blog.repository.UserRepository;
import com.springboot.blog.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserManagementControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ForgotPasswordTokenRepository forgotPasswordTokenRepository;

    @MockBean
    private EmailService emailService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.user-management.token-expiry-in-minutes}")
    private long expiryInMinutes;

    private final Faker faker = new Faker();
    private final String baseUrl = "/user-management";

    @Test
    void whenForgotPasswordForValidUser_ShouldSendToken() throws Exception {
        User user = createUser();

        ArgumentCaptor<EmailDto> emailDtoArgumentCaptor = ArgumentCaptor.forClass(EmailDto.class);
        Mockito.doNothing().when(emailService).sendEmail(emailDtoArgumentCaptor.capture());

        LocalDateTime now = LocalDateTime.now();

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(user.getEmail());

        String expectedMessage = String.format(
                "A token has been sent to your email: %s. It will expire in %s minutes.", user.getEmail(), expiryInMinutes
        );

        mockMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(expectedMessage))
                .andDo(MockMvcResultHandlers.print());

        ForgotPasswordToken forgotPasswordToken = forgotPasswordTokenRepository.findByUser(user)
                .orElse(null);
        assertThat(forgotPasswordToken).isNotNull();

        assertThat(forgotPasswordToken.getTokenHash()).isNotNull();

        long expiryMins = ChronoUnit.MINUTES.between(now, forgotPasswordToken.getExpiryTime());
        assertThat(expiryMins).isEqualTo(expiryInMinutes);

        EmailDto emailDto = emailDtoArgumentCaptor.getValue();
        assertThat(user.getEmail()).isEqualTo(emailDto.getRecipientEmail());
        assertThat(emailDto.getSubject()).isEqualTo("Reset Email Token");
        assertThat(emailDto.getMessage()).contains(String.valueOf(expiryInMinutes));
    }


    @Test
    void whenForgotPasswordForInvalidUserEmail_ShouldReturn4xx() throws Exception {
        String invalidEmail = generateInvalidEmail();

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(invalidEmail);

        mockMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Email not found"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void whenResetPasswordWithValidToken_ShouldSucceed() throws Exception {
        User user = createUser();
        String token = faker.lorem().word();
        String newPassword = faker.lorem().word();

        createForgotPasswordToken(user, token);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail(user.getEmail());
        request.setToken(token);
        request.setNewPassword(newPassword);
        request.setConfirmPassword(newPassword);

        mockMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Your password has been reset successfully"))
                .andDo(MockMvcResultHandlers.print());

        ForgotPasswordToken forgotPasswordToken = forgotPasswordTokenRepository.findByUser(user)
                .orElse(null);
        assertThat(forgotPasswordToken).isNotNull();
        assertThat(forgotPasswordToken.isUsed()).isTrue();

        User updatedUser = userRepository.findById(user.getId())
                .orElse(null);
        assertThat(updatedUser).isNotNull();

        boolean passwordMatches = passwordEncoder.matches(newPassword, updatedUser.getPassword());
        assertThat(passwordMatches).isTrue();
    }

    @Test
    void whenResetPasswordWithoutExistingToken_ShouldReturn4xx() throws Exception {
        User user = createUser();
        String token = faker.lorem().word();
        String newPassword = faker.lorem().word();

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail(user.getEmail());
        request.setToken(token);
        request.setNewPassword(newPassword);
        request.setConfirmPassword(newPassword);

        mockMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Token does not exist for this user"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void whenResetPasswordWithInvalidToken_ShouldReturn4xx() throws Exception {
        User user = createUser();
        String token = faker.lorem().word();
        String newPassword = faker.lorem().word();

        createForgotPasswordToken(user, token);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail(user.getEmail());
        request.setToken(token + faker.lorem().characters(1));
        request.setNewPassword(newPassword);
        request.setConfirmPassword(newPassword);

        mockMvc.perform(MockMvcRequestBuilders.post(baseUrl + "/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Invalid token submitted"))
                .andDo(MockMvcResultHandlers.print());
    }

    private User createUser() {
        User user = User.builder()
                .email(faker.internet().emailAddress())
                .password(faker.lorem().characters(30))
                .build();
        return userRepository.save(user);
    }

    private void createForgotPasswordToken(User user, String token) {
        createForgotPasswordToken(user, token, LocalDateTime.now().plusMinutes(expiryInMinutes));
    }

    private void createForgotPasswordToken(User user, String token, LocalDateTime expiryTime) {
        ForgotPasswordToken forgotPasswordToken = ForgotPasswordToken.builder()
                .user(user)
                .tokenHash(passwordEncoder.encode(token))
                .expiryTime(expiryTime)
                .build();
        forgotPasswordTokenRepository.save(forgotPasswordToken);
    }

    private String generateInvalidEmail() {
        String invalidEmail;
        boolean emailFound;
        do {
            invalidEmail = faker.lorem().word();
            emailFound = userRepository.findByEmail(invalidEmail).isPresent();
        } while (emailFound);
        return invalidEmail;
    }
}