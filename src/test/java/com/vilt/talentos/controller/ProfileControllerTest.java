package com.vilt.talentos.controller;

import com.vilt.talentos.dto.ProfileRequest;
import com.vilt.talentos.entity.Profile;
import com.vilt.talentos.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest extends BaseControllerTest {

    @MockBean
    private ProfileService profileService;

    @Test
    @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000")
    void getMyProfile_Authenticated_ReturnsProfile() throws Exception {
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Profile profile = Profile.builder()
                .id(UUID.randomUUID())
                .jobTitle("Developer")
                .build();

        when(profileService.getByUserId(userId)).thenReturn(profile);

        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("Developer"));
    }

    @Test
    @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000")
    void submit_ValidRequest_ReturnsUpdatedProfile() throws Exception {
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ProfileRequest req = new ProfileRequest(null, "Senior Developer", "Engineering", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, Collections.emptyList());
        Profile profile = Profile.builder()
                .id(UUID.randomUUID())
                .jobTitle("Senior Developer")
                .build();

        when(profileService.createOrUpdate(eq(userId), any(ProfileRequest.class))).thenReturn(profile);

        mockMvc.perform(post("/api/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("Senior Developer"));
    }
}
