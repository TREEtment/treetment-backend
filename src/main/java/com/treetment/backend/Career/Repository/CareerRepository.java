package com.treetment.backend.Career.Repository;

import com.treetment.backend.entity.Career;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CareerRepository extends JpaRepository<Career, Long> {
    /**
     * 특정 상담사 ID에 해당하는 모든 경력을 조회하는 메서드
     * @param counselorId 조회할 상담사의 ID
     * @return 해당 상담사의 모든 경력 리스트
     */
    List<Career> findByCounselorId(Long counselorId);
}
