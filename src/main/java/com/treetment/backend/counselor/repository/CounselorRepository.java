package com.treetment.backend.counselor.Repository;

import com.treetment.backend.counselor.entity.Counselor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Counselor 엔티티에 대한 데이터베이스 접근을 담당하는 Repo
@Repository
public interface CounselorRepository extends JpaRepository<Counselor, Long> {
}
