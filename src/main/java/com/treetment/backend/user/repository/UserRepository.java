package com.treetment.backend.user.repository;

import com.treetment.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Boolean existsByNickname(String nickname);
    Boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
