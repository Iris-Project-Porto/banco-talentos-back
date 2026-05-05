package com.vilt.talentos.repository;

import com.vilt.talentos.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByUserId(UUID userId);

    @Query("SELECT p FROM Profile p WHERE p.status = ?1")
    List<Profile> findByStatus(String status);
}
