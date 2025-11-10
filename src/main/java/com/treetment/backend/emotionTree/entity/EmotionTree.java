package com.treetment.backend.emotionTree.entity;

import com.treetment.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "emotion_tree")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EmotionTree {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    // 추가: 단순 조회/전달 편의를 위한 userId 필드 (동일 컬럼 매핑, 읽기 전용)
    @Column(name = "user_id", insertable = false, updatable = false)
    private Integer userId;
    
    @Column(name = "emotion_tree_image")
    private String emotionTreeImage;

    @Column(name = "render_status")
    private String renderStatus; // "rendering", "done", "failed"

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "model_url")
    private String modelUrl;

    @Column(name = "data_url")
    private String dataUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateImage(String emotionTreeImage) {
        this.emotionTreeImage = emotionTreeImage;
    }

    public void updateRenderStatus(String renderStatus) {
        this.renderStatus = renderStatus;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void completeRender(String imageUrl) {
        this.renderStatus = "done";
        this.imageUrl = imageUrl;
    }

    public void markRendering() {
        this.renderStatus = "rendering";
        this.updatedAt = LocalDateTime.now();
    }

    public void markDone(String imageUrl) {
        this.renderStatus = "done";
        this.imageUrl = imageUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public void markDoneWithModel(String modelUrl, String dataUrl) {
        this.renderStatus = "done";
        this.modelUrl = modelUrl;
        this.dataUrl = dataUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.renderStatus = "failed";
        this.updatedAt = LocalDateTime.now();
    }
}
