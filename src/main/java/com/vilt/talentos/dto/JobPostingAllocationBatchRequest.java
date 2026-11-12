package com.vilt.talentos.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JobPostingAllocationBatchRequest(
    @Valid
    List<JobPostingAllocationItemRequest> allocations,
    UUID resourceId,
    LocalDate startDate,
    String role
) {
    public JobPostingAllocationBatchRequest(List<JobPostingAllocationItemRequest> allocations) {
        this(allocations, null, null, null);
    }

    public JobPostingAllocationBatchRequest(UUID resourceId, LocalDate startDate, String role) {
        this(null, resourceId, startDate, role);
    }

    @AssertTrue(message = "Informe ao menos uma alocação válida (lista 'allocations' ou campos 'resourceId', 'startDate' e 'role')")
    @JsonIgnore
    public boolean isPayloadValid() {
        if (allocations != null) {
            return !allocations.isEmpty();
        }
        return resourceId != null
                && startDate != null
                && role != null
                && !role.isBlank();
    }

    @JsonIgnore
    public List<JobPostingAllocationItemRequest> resolvedAllocations() {
        if (allocations != null && !allocations.isEmpty()) {
            return allocations;
        }
        return List.of(new JobPostingAllocationItemRequest(resourceId, startDate, role));
    }
}
