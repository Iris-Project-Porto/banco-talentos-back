package com.vilt.talentos.repository;

import com.vilt.talentos.dto.JobPostingFilterRequest;
import com.vilt.talentos.entity.JobPosting;
import com.vilt.talentos.entity.JobPostingStatus;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class JobPostingSpecification {

    public static Specification<JobPosting> withFilters(boolean active, JobPostingFilterRequest filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (Long.class != query.getResultType()) {
                root.fetch("project", JoinType.LEFT);
                root.fetch("squad", JoinType.LEFT);
                root.fetch("skills", JoinType.LEFT).fetch("skill", JoinType.LEFT);
                query.distinct(true);
            }

            if (active) {
                predicates.add(root.get("status").in(
                        JobPostingStatus.OPEN,
                        JobPostingStatus.SCREENING,
                        JobPostingStatus.ALLOCATING));
            } else {
                predicates.add(root.get("status").in(
                        JobPostingStatus.FILLED,
                        JobPostingStatus.CLOSED,
                        JobPostingStatus.CANCELLED));
            }

            if (filters == null)
                return cb.and(predicates.toArray(new Predicate[0]));

            // busca textual
            if (StringUtils.hasText(filters.vacancyCode())) {
                predicates.add(
                        cb.like(cb.lower(root.get("vacancyCode")), "%" + filters.vacancyCode().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(filters.title())) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + filters.title().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(filters.recruiter())) {
                predicates.add(cb.like(cb.lower(root.get("recruiter")), "%" + filters.recruiter().toLowerCase() + "%"));
            }

            if (filters.projectId() != null) {
                predicates.add(cb.equal(root.get("project").get("id"), filters.projectId()));
            }
            if (filters.squadId() != null) {
                predicates.add(cb.equal(root.get("squad").get("id"), filters.squadId()));
            }
            if (filters.status() != null) {
                predicates.add(cb.equal(root.get("status"), filters.status()));
            }
            if (StringUtils.hasText(filters.experienceLevel())) {
                predicates.add(cb.equal(root.get("experienceLevel"), filters.experienceLevel()));
            }
            if (StringUtils.hasText(filters.modality())) {
                predicates.add(cb.equal(root.get("modality"), filters.modality()));
            }

            // Filtro multiplo para Skills
            if (filters.skills() != null && !filters.skills().isEmpty()) {
                var skillsJoin = root.join("skills").join("skill");

                List<Predicate> skillPredicates = new ArrayList<>();

                for (String skill : filters.skills()) {
                    String likePattern = "%" + skill.toLowerCase().trim() + "%";
                    skillPredicates.add(cb.like(cb.lower(skillsJoin.get("name")), likePattern));
                }

                predicates.add(cb.or(skillPredicates.toArray(new Predicate[0])));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}