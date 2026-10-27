package com.vilt.talentos.repository;

import com.vilt.talentos.entity.JobPosting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // Desassocia as vagas da squad antes de sua exclusão
    @Modifying
    @Query("update JobPosting j set j.squad = null where j.squad.id = :squadId")
    int clearSquad(@Param("squadId") UUID squadId);

    // Desassocia as vagas do projeto antes de sua exclusão
    @Modifying
    @Query("update JobPosting j set j.project = null where j.project.id = :projectId")
    int clearProject(@Param("projectId") UUID projectId);
}
