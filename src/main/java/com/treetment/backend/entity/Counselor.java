package com.treetment.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Counselor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String introduction;
    
    private String comment;
    
    private String contactAddress;
    
    @Builder.Default
    private Float score = 0.0f;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Career> careers;
    
    @OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CounselorReview> counselorReviews;
    
}
