package com.vilt.talentos.repository;

import com.vilt.talentos.entity.FormSubmission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FormSubmissionRepository extends JpaRepository<FormSubmission, UUID> {
    @Override
    @EntityGraph(attributePaths = {"formDefinition", "user"})
    List<FormSubmission> findAll();
}
