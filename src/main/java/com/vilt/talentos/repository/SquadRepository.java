package com.vilt.talentos.repository;

import com.vilt.talentos.entity.Squad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SquadRepository extends JpaRepository<Squad, UUID> {
    List<Squad> findByActive(boolean active);
    List<Squad> findByProjectId(UUID projectId);
}
