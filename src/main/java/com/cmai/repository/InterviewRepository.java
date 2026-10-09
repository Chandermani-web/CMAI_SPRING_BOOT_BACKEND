package com.cmai.repository;

import com.cmai.entity.Interview;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    Optional<Interview> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = "questions")
    List<Interview> findByUserIdOrderByCreatedAtDesc(Long userId);
}
