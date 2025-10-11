package com.treetment.backend.Career.Service;

import com.treetment.backend.Career.DTO.CareerRequestDTO;
import com.treetment.backend.Career.DTO.CareerResponseDTO;
import com.treetment.backend.Career.Repository.CareerRepository;
import com.treetment.backend.Counselor.Repository.CounselorRepository;
import com.treetment.backend.entity.Career;
import com.treetment.backend.entity.Counselor;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CareerService {
    private final CareerRepository careerRepository;
    private final CounselorRepository counselorRepository; // Counselor를 찾기 위해 필요

    // 경력 생성
    @Transactional
    public CareerResponseDTO createCareer(CareerRequestDTO requestDTO) {
        // 요청 DTO에서 counselorId를 이용해 Counselor 엔티티를 찾음
        Counselor counselor = counselorRepository.findById(requestDTO.getCounselorId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 ID의 상담사를 찾을 수 없습니다: " + requestDTO.getCounselorId()));

        // 새로운 Career 엔티티를 생성하고 연관관계를 설정
        Career career = Career.builder()
                .counselor(counselor)
                .careerContent(requestDTO.getCareerContent())
                .build();

        // DB에 저장하고 DTO로 변환하여 반환
        Career savedCareer = careerRepository.save(career);
        return CareerResponseDTO.from(savedCareer);
    }

    // 특정 상담사의 모든 경력 조회
    @Transactional(readOnly = true)
    public List<CareerResponseDTO> getCareersByCounselorId(Long counselorId) {
        List<Career> careers = careerRepository.findByCounselorId(counselorId);
        return careers.stream()
                .map(CareerResponseDTO  ::from)
                .toList(); // collect(Collectors.toList())
    }

    // 경력 내용 수정
    @Transactional
    public CareerResponseDTO updateCareer(Long careerId, String newContent) {
        Career career = careerRepository.findById(careerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 ID의 경력을 찾을 수 없습니다: " + careerId));

        career.setCareerContent(newContent);
        // @Transactional에 의해 메서드 종료 시 자동으로 update 쿼리가 실행
        return CareerResponseDTO.from(career);
    }

    // 경력 삭제
    @Transactional
    public void deleteCareer(Long careerId) {
        if (!careerRepository.existsById(careerId)) {
            throw new EntityNotFoundException(
                    "해당 ID의 경력을 찾을 수 없습니다: " + careerId);
        }
        careerRepository.deleteById(careerId);
    }
}
