package com.vilt.talentos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vilt.talentos.entity.JobPostingSkill;

import java.util.UUID;

@Repository
public interface JobPostingSkillRepository extends JpaRepository<JobPostingSkill, UUID> {
    
    void deleteByJobPostingId(UUID jobPostingId);
}