package com.springboot.blog.repository;

import com.springboot.blog.entity.ForgotPasswordToken;
import com.springboot.blog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface ForgotPasswordTokenRepository extends JpaRepository<ForgotPasswordToken, Long> {

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    void deleteByUserId(Long userId);

    Optional<ForgotPasswordToken> findByUser(User user);
}
