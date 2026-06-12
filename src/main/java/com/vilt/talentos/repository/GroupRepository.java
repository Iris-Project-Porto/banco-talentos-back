package com.vilt.talentos.repository;

import com.vilt.talentos.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    Optional<Group> findByName(String name);
    Page<Group> findByActive(boolean active, Pageable pageable);
    
    @Override
    Page<Group> findAll(Pageable pageable);
}
