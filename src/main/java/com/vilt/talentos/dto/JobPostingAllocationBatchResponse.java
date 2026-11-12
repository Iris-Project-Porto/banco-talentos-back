package com.vilt.talentos.dto;

import java.util.List;

public record JobPostingAllocationBatchResponse(
    JobPostingResponse jobPosting,
    List<JobPostingAllocationResponse> allocations
) {}

