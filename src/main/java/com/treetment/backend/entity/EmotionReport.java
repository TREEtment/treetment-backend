package com.treetment.backend.entity;

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
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reportContent;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDate createdAt;
    
}
