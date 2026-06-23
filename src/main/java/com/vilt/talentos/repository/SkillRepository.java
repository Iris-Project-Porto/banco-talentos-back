package com.vilt.talentos.repository;

import com.vilt.talentos.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID>, JpaSpecificationExecutor<Skill> {

    Optional<Skill> findByName(String name);

    Page<Skill> findByActive(boolean active, Pageable pageable);

    @Override
    Page<Skill> findAll(Pageable pageable);
}
