package com.treetment.backend.emotionTree.entity;

import com.treetment.backend.user.entity.User;
import com.treetment.backend.global.entity.CreateUpdateAt;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionTree {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;
    
    private String emotionTreeImage;

    @Embedded
    private CreateUpdateAt createUpdateAt;

    public void updateImage(String emotionTreeImage) {
        this.emotionTreeImage = emotionTreeImage;
    }
}
