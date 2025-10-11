package com.treetment.backend.CounselorReview.Repository;

import com.treetment.backend.entity.CounselorReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CounselorReviewRepository extends JpaRepository<CounselorReview, Long> {
    /**
     * 특정 상담사 ID에 해당하는 모든 리뷰를 조회하는 메서드
     * @param counselorId 조회할 상담사의 ID
     * @return 해당 상담사의 리뷰 리스트
     */
    List<CounselorReview> findByCounselorId(Long counselorId);
}
