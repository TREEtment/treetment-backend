package com.treetment.backend.Counselor.Repository;

import com.treetment.backend.entity.Counselor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CounselorRepository extends JpaRepository<Counselor, Long> {
}
