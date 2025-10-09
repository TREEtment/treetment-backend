package com.treetment.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
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

    @Lob
    @Column(columnDefinition = "TEXT")
    private String gptAnswer;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
}
