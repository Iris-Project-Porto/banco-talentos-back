package com.vilt.talentos.service;

import com.vilt.talentos.dto.AdminSkillListResponse;
import com.vilt.talentos.dto.AvatarResponse;
import com.vilt.talentos.entity.AvatarType;
import com.vilt.talentos.entity.Profile;
import com.vilt.talentos.entity.ProfileSkill;
import com.vilt.talentos.entity.Skill;
import com.vilt.talentos.entity.SkillCategory;
import com.vilt.talentos.entity.SkillType;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.mapper.SkillMapper;
import com.vilt.talentos.repository.SkillRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepo;

    @Mock
    private ProfileService profileService;

    @Mock
    private SkillMapper mapper;

    @InjectMocks
    private SkillService skillService;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "null", "NULL", "Null", "undefined", "UNDEFINED", " undefined "})
    @DisplayName("isValidPhotoUrl deve retornar false para null, vazio, em branco ou strings literais null/undefined")
    void isValidPhotoUrl_shouldReturnFalseForInvalidUrls(String photoUrl) {
        assertFalse(skillService.isValidPhotoUrl(photoUrl));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://example.com/photo.jpg",
            "http://zeus.vilt-group.com/file/view/47887",
            "https://avatars.githubusercontent.com/u/339265849?v=4"
    })
    @DisplayName("isValidPhotoUrl deve retornar true para URLs válidas")
    void isValidPhotoUrl_shouldReturnTrueForValidUrls(String photoUrl) {
        assertTrue(skillService.isValidPhotoUrl(photoUrl));
    }

    @Test
    @DisplayName("getAdminSkills: deve retornar PHOTO quando photoUrl é uma URL válida")
    void getAdminSkills_shouldReturnPhotoWhenUrlIsValid() {
        String validUrl = "https://example.com/avatar.jpg";
        Skill skill = createSkillWithResource("João Silva", validUrl, 5);

        Page<Skill> skillPage = new PageImpl<>(List.of(skill));
        when(skillRepo.findAll(any(Example.class), any(Pageable.class))).thenReturn(skillPage);

        Page<AdminSkillListResponse> response = skillService.getAdminSkills(null, null, PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1, response.getContent().size());

        List<AvatarResponse> avatars = response.getContent().get(0).avatars();
        assertEquals(1, avatars.size());
        assertEquals(AvatarType.PHOTO, avatars.get(0).type());
        assertEquals(validUrl, avatars.get(0).value());
    }

    @Test
    @DisplayName("getAdminSkills: deve retornar INITIALS com iniciais do recurso quando photoUrl é string vazia")
    void getAdminSkills_shouldReturnInitialsWhenPhotoUrlIsEmpty() {
        Skill skill = createSkillWithResource("Diogo Carrasco", "", 4);

        Page<Skill> skillPage = new PageImpl<>(List.of(skill));
        when(skillRepo.findAll(any(Example.class), any(Pageable.class))).thenReturn(skillPage);
        when(profileService.getInitials("Diogo Carrasco")).thenReturn("DC");

        Page<AdminSkillListResponse> response = skillService.getAdminSkills(null, null, PageRequest.of(0, 10));

        assertNotNull(response);
        List<AvatarResponse> avatars = response.getContent().get(0).avatars();
        assertEquals(1, avatars.size());
        assertEquals(AvatarType.INITIALS, avatars.get(0).type());
        assertEquals("DC", avatars.get(0).value());
    }

    @Test
    @DisplayName("getAdminSkills: deve retornar INITIALS com iniciais do recurso quando photoUrl é string literal 'null'")
    void getAdminSkills_shouldReturnInitialsWhenPhotoUrlIsStringNull() {
        Skill skill = createSkillWithResource("Diogo Bessa", "null", 4);

        Page<Skill> skillPage = new PageImpl<>(List.of(skill));
        when(skillRepo.findAll(any(Example.class), any(Pageable.class))).thenReturn(skillPage);
        when(profileService.getInitials("Diogo Bessa")).thenReturn("DB");

        Page<AdminSkillListResponse> response = skillService.getAdminSkills(null, null, PageRequest.of(0, 10));

        assertNotNull(response);
        List<AvatarResponse> avatars = response.getContent().get(0).avatars();
        assertEquals(1, avatars.size());
        assertEquals(AvatarType.INITIALS, avatars.get(0).type());
        assertEquals("DB", avatars.get(0).value());
    }

    @Test
    @DisplayName("getAdminSkills: deve retornar INITIALS com iniciais do recurso quando photoUrl é string literal 'undefined'")
    void getAdminSkills_shouldReturnInitialsWhenPhotoUrlIsStringUndefined() {
        Skill skill = createSkillWithResource("Leticia", "undefined", 3);

        Page<Skill> skillPage = new PageImpl<>(List.of(skill));
        when(skillRepo.findAll(any(Example.class), any(Pageable.class))).thenReturn(skillPage);
        when(profileService.getInitials("Leticia")).thenReturn("L");

        Page<AdminSkillListResponse> response = skillService.getAdminSkills(null, null, PageRequest.of(0, 10));

        assertNotNull(response);
        List<AvatarResponse> avatars = response.getContent().get(0).avatars();
        assertEquals(1, avatars.size());
        assertEquals(AvatarType.INITIALS, avatars.get(0).type());
        assertEquals("L", avatars.get(0).value());
    }

    @Test
    @DisplayName("getAdminSkills: deve tratar profile com User nulo ou nome nulo com segurança sem lançar exceção")
    void getAdminSkills_shouldHandleNullUserOrNameSafely() {
        Profile profile = Profile.builder()
                .id(UUID.randomUUID())
                .photoUrl(null)
                .user(null)
                .build();

        ProfileSkill ps = ProfileSkill.builder()
                .profile(profile)
                .proficiencyLevel(3)
                .build();

        Skill skill = Skill.builder()
                .id(UUID.randomUUID())
                .name("REACT")
                .type(SkillType.HARD)
                .category(SkillCategory.FRONTEND)
                .active(true)
                .profileSkills(new ArrayList<>(List.of(ps)))
                .build();

        Page<Skill> skillPage = new PageImpl<>(List.of(skill));
        when(skillRepo.findAll(any(Example.class), any(Pageable.class))).thenReturn(skillPage);
        when(profileService.getInitials("")).thenReturn("");

        Page<AdminSkillListResponse> response = skillService.getAdminSkills(null, null, PageRequest.of(0, 10));

        assertNotNull(response);
        List<AvatarResponse> avatars = response.getContent().get(0).avatars();
        assertEquals(1, avatars.size());
        assertEquals(AvatarType.INITIALS, avatars.get(0).type());
        assertEquals("", avatars.get(0).value());
    }

    private Skill createSkillWithResource(String userName, String photoUrl, Integer level) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name(userName)
                .email(userName.toLowerCase().replace(" ", ".") + "@vilt-group.com")
                .build();

        Profile profile = Profile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .photoUrl(photoUrl)
                .build();

        ProfileSkill ps = ProfileSkill.builder()
                .profile(profile)
                .proficiencyLevel(level)
                .build();

        return Skill.builder()
                .id(UUID.randomUUID())
                .name("JAVA")
                .type(SkillType.HARD)
                .category(SkillCategory.BACKEND)
                .active(true)
                .profileSkills(new ArrayList<>(List.of(ps)))
                .build();
    }
}
