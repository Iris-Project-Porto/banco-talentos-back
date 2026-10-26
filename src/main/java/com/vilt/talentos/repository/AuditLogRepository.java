package com.vilt.talentos.repository;

import com.vilt.talentos.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    void deleteByEntityId(UUID entityId);
}
