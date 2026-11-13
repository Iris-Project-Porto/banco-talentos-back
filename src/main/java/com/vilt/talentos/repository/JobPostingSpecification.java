package com.vilt.talentos.repository;

import com.vilt.talentos.dto.JobPostingFilterRequest;
import com.vilt.talentos.entity.JobPosting;
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

            predicates.add(cb.equal(root.get("active"), active));

            if (filters == null) return cb.and(predicates.toArray(new Predicate[0]));

            //busca textual
            if (StringUtils.hasText(filters.vacancyCode())) {
                predicates.add(cb.like(cb.lower(root.get("vacancyCode")), "%" + filters.vacancyCode().toLowerCase() + "%"));
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
                predicates.add(skillsJoin.get("name").in(filters.skills()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}