package com.vilt.talentos.service;

import com.vilt.talentos.config.AppProperties;
import com.vilt.talentos.dto.ProfileRequest;
import com.vilt.talentos.entity.DomainStatus;
import com.vilt.talentos.entity.Profile;
import com.vilt.talentos.entity.RegistrationStatus;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.mapper.ProfileMapper;
import com.vilt.talentos.repository.GroupRepository;
import com.vilt.talentos.repository.ProfileRepository;
import com.vilt.talentos.repository.SkillRepository;
import com.vilt.talentos.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private TalentEvaluationService evaluationService;
    @Mock
    private ProfileMapper profileMapper;
    @Mock
    private EmailService emailService;
    @Mock
    private AppProperties appProperties;
    @Mock
    private SkillRepository skillRepo;
    @Mock
    private GroupRepository groupRepo;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void createOrUpdate_WhenProfileIsActive_ShouldBecomePending() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("test@vilt-group.com").build();
        Profile profile = Profile.builder().user(user).status(DomainStatus.ACTIVE).build();
        ProfileRequest req = new ProfileRequest(null, "Dev", "IT", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(profileRepo.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(evaluationService.evaluate(any())).thenReturn(new TalentEvaluationService.EvaluationResult(TalentEvaluationService.Nivel.PLENO, 50, "Justification"));
        when(profileRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Profile result = profileService.createOrUpdate(userId, req);

        assertEquals(DomainStatus.PENDING, result.getStatus(), "Profile status should be PENDING after update by resource");
    }

    @Test
    void createOrUpdate_WhenRegistrationNumberProvided_ShouldBeAwaitingApproval() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("test@vilt-group.com").build();
        Profile profile = Profile.builder().user(user).status(DomainStatus.PENDING).build();
        ProfileRequest req = new ProfileRequest(null, "Dev", "IT", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "12345", null, null, null);

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(profileRepo.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(evaluationService.evaluate(any())).thenReturn(new TalentEvaluationService.EvaluationResult(TalentEvaluationService.Nivel.PLENO, 50, "Justification"));
        when(profileRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Profile result = profileService.createOrUpdate(userId, req);

        assertEquals(RegistrationStatus.AWAITING_APPROVAL, result.getRegistrationStatus(), "Registration status should be AWAITING_APPROVAL");
    }
}
