package com.vilt.talentos.repository;

import com.vilt.talentos.entity.AuditLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    void deleteByEntityId(UUID entityId);

    @Override
    @EntityGraph(attributePaths = {"user", "userAffected"})
    Optional<AuditLog> findById(UUID id);

    @EntityGraph(attributePaths = {"user", "userAffected"})
    List<AuditLog> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"user", "userAffected"})
    List<AuditLog> findByEntityIdOrderByCreatedAtDesc(UUID entityId);

    @EntityGraph(attributePaths = {"user", "userAffected"})
    List<AuditLog> findByUserAffectedIdOrderByCreatedAtDesc(UUID userAffectedId);
}
