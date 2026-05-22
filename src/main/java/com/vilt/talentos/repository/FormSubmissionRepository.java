package com.vilt.talentos.repository;

import com.vilt.talentos.entity.FormDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FormDefinitonRepository extends JpaRepository<FormDefinition, UUID> {
}
