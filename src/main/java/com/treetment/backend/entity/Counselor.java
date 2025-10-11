package com.treetment.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Counselor extends CreateUpdateAt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;

    @Lob
    private String introduction;

    @Lob
    private String comment;
    
    private String contactAddress;
    
    @Builder.Default
    private float score = 0.0f;
    
    @OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Career> careers = new ArrayList<>();
    
    @OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CounselorReview> counselorReviews;
    
}
