package com.vilt.talentos.repository;

import com.vilt.talentos.entity.FormDefinition;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FormDefinitionRepository extends JpaRepository<FormDefinition, UUID> {
    @EntityGraph(attributePaths = {"group"})
    List<FormDefinition> findAllByGroup_Id(UUID groupId);

    @Override
    @EntityGraph(attributePaths = {"group"})
    List<FormDefinition> findAll();
}
