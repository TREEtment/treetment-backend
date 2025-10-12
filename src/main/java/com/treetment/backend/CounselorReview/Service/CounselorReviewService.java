package com.treetment.backend.CounselorReview.Service;

import com.treetment.backend.Counselor.Repository.CounselorRepository;
import com.treetment.backend.CounselorReview.DTO.CounselorReviewRequestDTO;
import com.treetment.backend.CounselorReview.DTO.CounselorReviewResponseDTO;
import com.treetment.backend.CounselorReview.Repository.CounselorReviewRepository;
import com.treetment.backend.entity.Counselor;
import com.treetment.backend.entity.CounselorReview;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CounselorReviewService {

    private final CounselorReviewRepository reviewRepository;
    private final CounselorRepository counselorRepository;

    // 리뷰 생성
    @Transactional
    public CounselorReviewResponseDTO createReview(CounselorReviewRequestDTO requestDTO) {
        Counselor counselor = counselorRepository.findById(requestDTO.getCounselorId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 ID의 상담사를 찾을 수 없습니다: " + requestDTO.getCounselorId()));

        CounselorReview review = CounselorReview.builder()
                .counselor(counselor)
                .score(requestDTO.getScore())
                .reviewTitle(requestDTO.getReviewTitle())
                .reviewContent(requestDTO.getReviewContent())
                .build();

        CounselorReview savedReview = reviewRepository.save(review);

        // 리뷰 저장 후 상담사 평균 별점 업데이트
        updateCounselorScore(counselor.getId());

        return CounselorReviewResponseDTO.from(savedReview);
    }

    // 특정 상담사의 모든 리뷰 조회
    @Transactional(readOnly = true)
    public List<CounselorReviewResponseDTO> getReviewsByCounselorId(Long counselorId) {
        if (!counselorRepository.existsById(counselorId)) {
            throw new EntityNotFoundException(
                    "해당 ID의 상담사를 찾을 수 없습니다: " + counselorId);
        }
        return reviewRepository.findByCounselorId(counselorId).stream()
                .map(CounselorReviewResponseDTO::from)
                .toList(); // collect(Collectors.toList())
    }

    // 리뷰 수정
    @Transactional
    public CounselorReviewResponseDTO updateReview(Long reviewId, CounselorReviewRequestDTO requestDTO) {
        CounselorReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 ID의 리뷰를 찾을 수 없습니다: " + reviewId));

        // 리뷰 내용 수정
        review.setScore(requestDTO.getScore());
        review.setReviewTitle(requestDTO.getReviewTitle());
        review.setReviewContent(requestDTO.getReviewContent());

        // 리뷰 수정 후 상담사 평균 별점 업데이트
        updateCounselorScore(review.getCounselor().getId());

        return CounselorReviewResponseDTO.from(review);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId) {
        CounselorReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 ID의 리뷰를 찾을 수 없습니다: " + reviewId));

        Long counselorId = review.getCounselor().getId();

        reviewRepository.delete(review);

        // 리뷰 삭제 후 상담사 평균 별점 업데이트
        updateCounselorScore(counselorId);
    }


    // 상담사 평균 별점을 계산하고 업데이트하는 내부 메서드
    private void updateCounselorScore(Long counselorId) {
        Counselor counselor = counselorRepository.findById(counselorId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 ID의 상담사를 찾을 수 없습니다: " + counselorId));

        List<CounselorReview> reviews = reviewRepository.findByCounselorId(counselorId);

        if (reviews.isEmpty()) {
            counselor.setScore(0.0f);
        } else {
            // 모든 리뷰의 점수 합계를 구한 뒤, 리뷰 개수로 나누어 평균을 계산
            double average = reviews.stream()
                    .mapToDouble(CounselorReview::getScore)
                    .average()
                    .orElse(0.0);

            // 소수점 첫째 자리까지 반올림
            float newScore = (float) (Math.round(average * 10) / 10.0);
            counselor.setScore(newScore);
        }
    }
}
