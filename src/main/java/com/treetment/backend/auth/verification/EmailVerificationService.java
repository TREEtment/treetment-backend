package com.treetment.backend.auth.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository verificationRepository;
    private final JavaMailSender mailSender;

    @Value("${verification.email.expire-minutes:10}")
    private long expireMinutes;

    private final SecureRandom random = new SecureRandom();

    @Transactional
    public void sendVerificationCode(String email) {
        String code = String.format("%06d", random.nextInt(1_000_000));

        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(expireMinutes))
                .verified(false)
                .build();
        verificationRepository.save(verification);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[TREEtment] 이메일 인증 코드");
        message.setText("인증 코드: " + code + "\n" + "유효시간: " + expireMinutes + "분");
        mailSender.send(message);
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        EmailVerification latest = verificationRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElse(null);
        if (latest == null) return false;
        if (latest.isExpired()) return false;
        if (!latest.getCode().equals(code)) return false;

        latest.setVerified(true);
        return true;
    }

    public boolean isEmailVerified(String email) {
        return verificationRepository.existsByEmailAndVerifiedIsTrue(email);
    }
}


