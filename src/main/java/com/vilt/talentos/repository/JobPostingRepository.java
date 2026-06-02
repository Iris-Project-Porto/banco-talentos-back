package com.vilt.talentos.repository;

import com.vilt.talentos.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobPostingRepository extends JpaRepository<JobPosting, UUID> {
    List<JobPosting> findByActive(boolean active);
}
