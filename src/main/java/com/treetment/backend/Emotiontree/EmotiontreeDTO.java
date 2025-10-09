package com.treetment.backend.Emotiontree;
import com.treetment.backend.entity.EmotionTree;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class EmotiontreeDTO {
    private final Long treeId;
    private final Integer userId;
    private final String emotionTreeImage;
    private final LocalDateTime createdAt;

    public EmotiontreeDTO(EmotionTree emotionTree)
    {
        this.treeId=emotionTree.getId();
        this.userId=emotionTree.getUser().getId();
        this.emotionTreeImage = emotionTree.getEmotionTreeImage();
        this.createdAt = emotionTree.getCreatedAt();
    }
}
