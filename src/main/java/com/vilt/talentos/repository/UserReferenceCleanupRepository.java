package com.vilt.talentos.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserReferenceCleanupRepository {

        private static final List<String> AUDIT_TABLES = List.of(
                        "profiles",
                        "skills",
                        "profile_skills",
                        "form_definitions",
                        "form_submissions",
                        "projects",
                        "squads",
                        "job_postings",
                        "job_posting_skills",
                        "resource_equipments");

        private final JdbcTemplate jdbcTemplate;

        // Limpa referências e submissões que impediriam a exclusão da conta do usuário.
        public void deleteReferences(UUID userId) {
                jdbcTemplate.update(
                                "UPDATE users SET created_by_id = NULL, updated_by_id = NULL, approved_by = NULL "
                                                + "WHERE created_by_id = ? OR updated_by_id = ? OR approved_by = ?",
                                userId, userId, userId);

                for (String table : AUDIT_TABLES) {
                        jdbcTemplate.update(
                                        "UPDATE " + table + " SET created_by_id = NULL, updated_by_id = NULL "
                                                        + "WHERE created_by_id = ? OR updated_by_id = ?",
                                        userId, userId);
                }

                jdbcTemplate.update("UPDATE audit_logs SET user_id = NULL WHERE user_id = ?", userId);
                jdbcTemplate.update("DELETE FROM form_submissions WHERE user_id = ?", userId);
        }
}
