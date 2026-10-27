package com.vilt.talentos.repository;

import com.vilt.talentos.entity.DomainStatus;
import com.vilt.talentos.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID>, JpaSpecificationExecutor<Profile> {
    @EntityGraph(attributePaths = {"user", "user.group", "skills", "skills.skill", "allocationProject", "allocationSquad", "equipments"})
    Optional<Profile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    boolean existsByCpf(String cpf);

    @EntityGraph(attributePaths = {"user", "user.group", "skills", "skills.skill", "allocationProject", "allocationSquad", "equipments"})
    Page<Profile> findByStatus(DomainStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "user.group", "skills", "skills.skill", "allocationProject", "allocationSquad", "equipments"})
    List<Profile> findByStatus(DomainStatus status);

    @Override
    @EntityGraph(attributePaths = {"user", "user.group", "skills", "skills.skill", "allocationProject", "allocationSquad", "equipments"})
    Page<Profile> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user", "user.group", "skills", "skills.skill", "allocationProject", "allocationSquad", "equipments"})
    List<Profile> findAll();

    @Override
    @EntityGraph(attributePaths = {"user", "user.group", "skills", "skills.skill", "allocationProject", "allocationSquad", "equipments"})
    Optional<Profile> findById(UUID id);

    // Exclui o perfil associado ao usuário informado.
    void deleteByUserId(UUID userId);

    @Modifying
    @Query("update Profile p set p.allocationSquad = null where p.allocationSquad.id = :squadId")
    int clearAllocationSquad(@Param("squadId") UUID squadId);

    @Modifying
    @Query("update Profile p set p.allocationProject = null where p.allocationProject.id = :projectId")
    int clearAllocationProject(@Param("projectId") UUID projectId);
}
