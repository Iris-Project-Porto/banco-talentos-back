package com.vilt.talentos.repository;

import com.vilt.talentos.dto.AuditEntityContext;
import com.vilt.talentos.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID>, JpaSpecificationExecutor<Skill> {

    Optional<Skill> findByName(String name);

    Page<Skill> findByActive(boolean active, Pageable pageable);

    @Override
    Page<Skill> findAll(Pageable pageable);

    @Query("select new com.vilt.talentos.dto.AuditEntityContext(s.id, s.name) " +
           "from Skill s where s.id in :ids")
    List<AuditEntityContext> findAuditContextsByIdIn(@Param("ids") Collection<UUID> ids);
}
