package com.treetment.backend.Emotiontree;

import com.treetment.backend.entity.EmotionTree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EmotiontreeRepository extends JpaRepository<EmotionTree, Long> {
    @Query("SELECT et FROM EmotionTree et JOIN FETCH et.user WHERE et.user.id = :userId")
    List<EmotionTree> findByUserIdWithUser(@Param("userId") Long userId);}
