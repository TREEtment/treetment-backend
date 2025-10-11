package com.treetment.backend.Counselor.Service;

import com.treetment.backend.Counselor.DTO.CounselorRequestDTO;
import com.treetment.backend.Counselor.DTO.CounselorResponseDTO;
import com.treetment.backend.entity.Career;
import com.treetment.backend.entity.Counselor;
import com.treetment.backend.Counselor.Repository.CounselorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성
public class CounselorService {

    private final CounselorRepository counselorRepository;

    // 상담사 생성
    @Transactional
    public CounselorResponseDTO createCounselor(CounselorRequestDTO requestDTO) {
        Counselor newCounselor = requestDTO.toEntity();
        Counselor savedCounselor = counselorRepository.save(newCounselor);
        return CounselorResponseDTO.from(savedCounselor);
    }

    // 전체 상담사 조회
    @Transactional(readOnly = true)
    public List<CounselorResponseDTO> getAllCounselors() {
        return counselorRepository.findAll().stream()
                .map(CounselorResponseDTO::from)
                .toList(); // collect(Collectors.toList())
    }

    // 특정 상담사 조회
    @Transactional(readOnly = true)
    public CounselorResponseDTO getCounselorById(Long id) {
        Counselor counselor = counselorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 ID의 상담사를 찾을 수 없습니다: " + id));
        return CounselorResponseDTO.from(counselor);
    }

    // 상담사 정보 수정
    @Transactional
    public CounselorResponseDTO updateCounselor(Long id, CounselorRequestDTO requestDTO) {
        // Id로 기존 상담사 조회
        Counselor existingCounselor = counselorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "해당 ID의 상담사를 찾을 수 없습니다: " + id));

        // 기본 정보 업데이트
        existingCounselor.setName(requestDTO.getName());
        existingCounselor.setIntroduction(requestDTO.getIntroduction());
        existingCounselor.setComment(requestDTO.getComment());
        existingCounselor.setContactAddress(requestDTO.getContactAddress());

        updateCareersForCounselor(existingCounselor, requestDTO.getCareers()); // 경력 정보 업데이트

        return CounselorResponseDTO.from(existingCounselor); // 업데이트된 상담사 정보를 DTO로 변환하여 반환
    }

    /**
     * 상담사의 경력 정보를 업데이트하는 메서드
     * @param counselor 업데이트할 상담사 엔티티
     * @param careerContents 새로운 경력 내용 리스트
     */
    private void updateCareersForCounselor(Counselor counselor, List<String> careerContents) {
        counselor.getCareers().clear(); // 기존 경력은 모두 삭제

        // 새로운 경력 내용이 있으면 추가
        if (careerContents != null && !careerContents.isEmpty()) {
            List<Career> updatedCareers = careerContents.stream()
                    .map(content -> Career.builder()
                            .careerContent(content)
                            .counselor(counselor)
                            .build())
                    .toList(); // .collect(Collectors.toList()) 대신 .toList() 사용
            counselor.getCareers().addAll(updatedCareers);
        }
    }

    // 상담사 삭제
    @Transactional
    public void deleteCounselor(Long id) {
        if (!counselorRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "해당 ID의 상담사를 찾을 수 없습니다: " + id);
        }
        counselorRepository.deleteById(id);
    }
}
