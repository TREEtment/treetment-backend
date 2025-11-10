package com.treetment.backend.emotionTree.repository;

import com.treetment.backend.emotionTree.entity.EmotionTree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface EmotiontreeRepository extends JpaRepository<EmotionTree, Long> {
    @Query("SELECT et FROM EmotionTree et JOIN FETCH et.user WHERE et.user.id = :userId")
    List<EmotionTree> findByUserIdWithUser(@Param("userId") Integer userId);
    Optional<EmotionTree> findByUser_Id(Integer userId);
    List<EmotionTree> findAllByUser_Id(Integer userId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM EmotionTree et WHERE et.user.id = :userId")
    int deleteByUser_Id(@Param("userId") Integer userId);
}
