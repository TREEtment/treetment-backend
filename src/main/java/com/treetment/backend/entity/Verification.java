package com.treetment.backend.entity;

import com.treetment.backend.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Verification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;
    
    @Column(nullable = false)
    private Integer verificationCode;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean isChecked = false;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean isValid = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
}
