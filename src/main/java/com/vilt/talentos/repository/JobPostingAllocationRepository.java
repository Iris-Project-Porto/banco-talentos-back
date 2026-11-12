package com.vilt.talentos.repository;

import com.vilt.talentos.entity.JobPostingAllocation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface JobPostingAllocationRepository extends JpaRepository<JobPostingAllocation, UUID> {

    @EntityGraph(attributePaths = {"jobPosting", "profile", "profile.user", "project", "squad", "createdBy"})
    List<JobPostingAllocation> findByJobPostingIdOrderByCreatedAtDesc(UUID jobPostingId);

    boolean existsByProfileIdAndActiveTrue(UUID profileId);

    @Modifying
    @Query("UPDATE JobPostingAllocation a SET a.active = false WHERE a.profile.id = :profileId AND a.active = true")
    int deactivateByProfileId(@Param("profileId") UUID profileId);

    @Modifying
    @Query("UPDATE JobPostingAllocation a SET a.active = false WHERE a.squad.id = :squadId AND a.active = true")
    int deactivateBySquadId(@Param("squadId") UUID squadId);

    @Modifying
    @Query("UPDATE JobPostingAllocation a SET a.active = false WHERE a.squad.id = :squadId AND a.profile.id NOT IN :profileIds AND a.active = true")
    int deactivateBySquadIdAndProfileIdNotIn(@Param("squadId") UUID squadId, @Param("profileIds") Collection<UUID> profileIds);

    @Modifying
    @Query("UPDATE JobPostingAllocation a SET a.active = false WHERE a.project.id = :projectId AND a.active = true")
    int deactivateByProjectId(@Param("projectId") UUID projectId);
}
