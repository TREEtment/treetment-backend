package com.treetment.backend.emotionRecord.entity;

import com.treetment.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
    
    private Float emotionScore;
    
    private String emotionImage;

    @Column(nullable = false)
    private String emotionTitle;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String emotionContent;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String gptAnswer;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
    public void update(String title, String content, float score, String gptAnswer) {
        this.emotionTitle = title;
        this.emotionContent = content;
        this.emotionScore = score;
        this.gptAnswer = gptAnswer;
    }
}
