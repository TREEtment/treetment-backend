package com.treetment.backend.entity;

import com.treetment.backend.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;
    
    @Column(nullable = false)
    private Float emotionScore;
    
    @Column(nullable = false)
    private String reportTitle;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String reportContent;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDate createdAt;

    public void updateContent(String reportTitle, String reportContent, float emotionScore) {
        this.reportTitle = reportTitle;
        this.reportContent = reportContent;
        this.emotionScore = emotionScore;
    }
    
}
