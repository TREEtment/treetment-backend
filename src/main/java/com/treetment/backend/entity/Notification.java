package com.treetment.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;
    
    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserNotification> userNotifications;
    
}
