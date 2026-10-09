package com.cmai.repository;

import com.cmai.entity.Roadmap;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    Optional<Roadmap> findByIdAndUserId(Long id, Long userId);

    List<Roadmap> findByUserIdOrderByCreatedAtDesc(Long userId);
}
