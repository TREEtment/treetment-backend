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

    // 1. 상담사 생성
    @Transactional
    public CounselorResponseDTO createCounselor(CounselorRequestDTO requestDTO) {
        Counselor newCounselor = requestDTO.toEntity();
        Counselor savedCounselor = counselorRepository.save(newCounselor);
        return CounselorResponseDTO.from(savedCounselor);
    }

    // 2. 전체 상담사 조회
    @Transactional(readOnly = true)
    public List<CounselorResponseDTO> getAllCounselors() {
        return counselorRepository.findAll().stream()
                .map(CounselorResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 3. 특정 상담사 조회
    @Transactional(readOnly = true)
    public CounselorResponseDTO getCounselorById(Long id) {
        Counselor counselor = counselorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상담사를 찾을 수 없습니다: " + id));
        return CounselorResponseDTO.from(counselor);
    }

    // 4. 상담사 정보 수정
    @Transactional
    public CounselorResponseDTO updateCounselor(Long id, CounselorRequestDTO requestDto) {
        Counselor existingCounselor = counselorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상담사를 찾을 수 없습니다: " + id));

        // 기본 정보 업데이트
        existingCounselor.setName(requestDto.getName());
        existingCounselor.setIntroduction(requestDto.getIntroduction());
        existingCounselor.setComment(requestDto.getComment());
        existingCounselor.setContactAddress(requestDto.getContactAddress());

        // 경력 정보 업데이트 (기존 경력 모두 삭제 후 새로 추가)
        existingCounselor.getCareers().clear();
        if (requestDto.getCareers() != null) {
            List<Career> updatedCareers = requestDto.getCareers().stream()
                    .map(careerContent -> Career.builder()
                            .careerContent(careerContent)
                            .counselor(existingCounselor)
                            .build())
                    .collect(Collectors.toList());
            existingCounselor.getCareers().addAll(updatedCareers);
        }

        // JpaRepository의 save는 id가 있으면 update, 없으면 insert
        // @Transactional 어노테이션 덕분에 메서드 종료 시 변경된 내용을 자동으로 DB에 반영(dirty checking)하므로
        // save를 호출할 필요는 없지만, 명확성을 위해 호출
        Counselor updatedCounselor = counselorRepository.save(existingCounselor);

        return CounselorResponseDTO.from(updatedCounselor);
    }

    // 5. 상담사 삭제
    @Transactional
    public void deleteCounselor(Long id) {
        if (!counselorRepository.existsById(id)) {
            throw new EntityNotFoundException("해당 ID의 상담사를 찾을 수 없습니다: " + id);
        }
        counselorRepository.deleteById(id);
    }
}
