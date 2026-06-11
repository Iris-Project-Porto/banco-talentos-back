package com.vilt.talentos.controller;

import com.vilt.talentos.dto.SkillRequest;
import com.vilt.talentos.dto.SkillResponse;
import com.vilt.talentos.entity.SkillType;
import com.vilt.talentos.service.SkillService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SkillController.class)
class SkillControllerTest extends BaseControllerTest {

    @MockBean
    private SkillService skillService;

    @Test
    @WithMockUser
    void getAllActive_WhenCalled_ReturnsActiveSkills() throws Exception {
        SkillResponse skill = new SkillResponse(UUID.randomUUID(), "JAVA", SkillType.HARD, true, 5);

        when(skillService.findAllActive(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(skill)));

        mockMvc.perform(get("/api/v1/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("JAVA"));
    }

    @Test
    @WithMockUser
    void create_ValidRequest_ReturnsCreatedSkill() throws Exception {
        SkillRequest req = new SkillRequest("Python", SkillType.HARD, 4);
        SkillResponse res = new SkillResponse(UUID.randomUUID(), "PYTHON", SkillType.HARD, true, 4);

        when(skillService.create(any(SkillRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("PYTHON"));
    }
}
