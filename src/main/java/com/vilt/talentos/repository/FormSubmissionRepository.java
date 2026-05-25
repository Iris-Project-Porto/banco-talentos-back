package com.vilt.talentos.repository;

import com.vilt.talentos.entity.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FormSubmissionRepository extends JpaRepository<FormSubmission, UUID> {
}
