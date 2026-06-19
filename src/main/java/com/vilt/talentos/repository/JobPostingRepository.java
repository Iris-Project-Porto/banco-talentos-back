package com.vilt.talentos.repository;

import com.vilt.talentos.entity.JobPosting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobPostingRepository extends JpaRepository<JobPosting, UUID> {
    @EntityGraph(attributePaths = {"project", "squad", "skills", "skills.skill"})
    Page<JobPosting> findByActive(boolean active, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"project", "squad", "skills", "skills.skill"})
    Page<JobPosting> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"project", "squad", "skills", "skills.skill"})
    java.util.Optional<JobPosting> findById(UUID id);
}
