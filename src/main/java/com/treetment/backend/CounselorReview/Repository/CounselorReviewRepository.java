package com.treetment.backend.CounselorReview.Repository;

import com.treetment.backend.entity.CounselorReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CounselorReviewRepository extends JpaRepository<CounselorReview, Long> {
    // 특정 상담사의 모든 리뷰를 찾는 쿼리 메서드
    List<CounselorReview> findByCounselorId(Long counselorId);
}
