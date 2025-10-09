package com.treetment.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CounselorReview extends CreateUpdateAt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Counselor counselor;
    
    @Column(nullable = false)
    private byte score;
    
    @Column(nullable = false)
    private String reviewTitle;
    
    @Column(nullable = false)
    private String reviewContent;
}
