package com.treetment.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Getter
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
    
    @Column(nullable = false)
    private String reportContent;
    
    @Column(nullable = false)
    private LocalDate createdAt;
    
}
