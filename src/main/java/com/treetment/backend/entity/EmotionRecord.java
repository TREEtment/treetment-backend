package com.treetment.backend.entity;

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
public class EmotionRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;
    
    private Float emotionScore;
    
    private String emotionImage;

    @Column(nullable = false)
    private String emotionTitle;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String emotionContent;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
}
