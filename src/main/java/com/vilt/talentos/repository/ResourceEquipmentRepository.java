package com.vilt.talentos.repository;

import com.vilt.talentos.dto.AuditEntityContext;
import com.vilt.talentos.entity.ResourceEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResourceEquipmentRepository extends JpaRepository<ResourceEquipment, UUID> {
    @Query("select new com.vilt.talentos.dto.AuditEntityContext(e.id, "
            + "coalesce(nullif(e.hostname, ''), nullif(e.assetNumber, ''), nullif(e.tag, ''), "
            + "nullif(e.brandOs, ''), nullif(e.processor, ''))) "
            + "from ResourceEquipment e where e.id in :ids")
    List<AuditEntityContext> findAuditContextsByIdIn(@Param("ids") Collection<UUID> ids);

    List<ResourceEquipment> findByProfileIdOrderByCreatedAtAsc(UUID profileId);

    Optional<ResourceEquipment> findByIdAndProfileId(UUID id, UUID profileId);
}
