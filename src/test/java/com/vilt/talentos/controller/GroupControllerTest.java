package com.vilt.talentos.controller;

import com.vilt.talentos.dto.GroupResponse;
import com.vilt.talentos.service.GroupService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
class GroupControllerTest extends BaseControllerTest {

    @MockBean
    private GroupService groupService;

    @Test
    void getAllGroups_WhenCalled_ReturnsActiveGroups() throws Exception {
        GroupResponse group = new GroupResponse(
                UUID.randomUUID(),
                "Engineering",
                "Software engineering group",
                true,
                Instant.now(),
                Instant.now(),
                "admin",
                "admin"
        );

        when(groupService.findAllActive(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(group)));

        mockMvc.perform(get("/api/v1/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Engineering"));
    }
}
