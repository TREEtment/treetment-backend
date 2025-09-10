package com.treetment.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long key;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Notification notification;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
}
