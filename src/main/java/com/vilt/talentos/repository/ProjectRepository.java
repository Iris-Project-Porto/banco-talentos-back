package com.vilt.talentos.repository;

import com.vilt.talentos.dto.AuditEntityContext;
import com.vilt.talentos.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    @Query("select new com.vilt.talentos.dto.AuditEntityContext(p.id, p.name) "
            + "from Project p where p.id in :ids")
    List<AuditEntityContext> findAuditContextsByIdIn(@Param("ids") Collection<UUID> ids);

    Page<Project> findByActive(boolean active, Pageable pageable);

    @EntityGraph(attributePaths = {"squads"})
    Optional<Project> findWithSquadsById(UUID id);

    @Override
    Page<Project> findAll(Pageable pageable);
}
