package com.vilt.talentos.service;

import com.vilt.talentos.dto.JobPostingCandidateMatchResponse;
import com.vilt.talentos.entity.*;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.mapper.JobPostingMapper;
import com.vilt.talentos.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobPostingServiceTest {

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private JobPostingRepository jobPostingRepo;

    @Mock
    private ProjectRepository projectRepo;

    @Mock
    private SquadRepository squadRepo;

    @Mock
    private SkillRepository skillRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private ProfileRepository profileRepo;

    @Mock
    private JobPostingMapper mapper;

    @InjectMocks
    private JobPostingService jobPostingService;

    private UUID jobPostingId;
    private JobPosting jobPosting;
    private Skill reactSkill;
    private Skill typeScriptSkill;
    private Skill tailwindSkill;

    @BeforeEach
    void setUp() {
        jobPostingId = UUID.randomUUID();

        reactSkill = Skill.builder().id(UUID.randomUUID()).name("React").type(SkillType.HARD).build();
        typeScriptSkill = Skill.builder().id(UUID.randomUUID()).name("TypeScript").type(SkillType.HARD).build();
        tailwindSkill = Skill.builder().id(UUID.randomUUID()).name("Tailwind").type(SkillType.HARD).build();

        JobPostingSkill reqReact = JobPostingSkill.builder()
                .id(UUID.randomUUID())
                .skill(reactSkill)
                .type(RequirementType.MANDATORY)
                .importanceWeight(3)
                .minLevel(SkillLevel.INTERMEDIATE)
                .build();

        JobPostingSkill reqTs = JobPostingSkill.builder()
                .id(UUID.randomUUID())
                .skill(typeScriptSkill)
                .type(RequirementType.MANDATORY)
                .importanceWeight(3)
                .minLevel(SkillLevel.INTERMEDIATE)
                .build();

        JobPostingSkill desTailwind = JobPostingSkill.builder()
                .id(UUID.randomUUID())
                .skill(tailwindSkill)
                .type(RequirementType.DESIRABLE)
                .importanceWeight(2)
                .minLevel(SkillLevel.BASIC)
                .build();

        jobPosting = JobPosting.builder()
                .id(jobPostingId)
                .title("Desenvolvedor Frontend")
                .skills(new ArrayList<>(List.of(reqReact, reqTs, desTailwind)))
                .build();
    }

    @Test
    @DisplayName("Deve calcular match de candidatos com sucesso e ordenar por ranking")
    void shouldCalculateCandidateMatchAndRanking() {
        User user1 = User.builder().id(UUID.randomUUID()).name("João Silva").status(DomainStatus.ACTIVE).build();
        Squad squad = Squad.builder().id(UUID.randomUUID()).name("Squad Pagamentos").build();

        Profile profile1 = Profile.builder()
                .id(UUID.randomUUID())
                .user(user1)
                .jobTitle("Engenheiro de Software Pleno")
                .allocationSquad(squad)
                .resourceStatus(ResourceStatus.AVAILABLE)
                .skills(List.of(
                        ProfileSkill.builder().skill(reactSkill).proficiencyLevel(4).build(),
                        ProfileSkill.builder().skill(typeScriptSkill).proficiencyLevel(4).build(),
                        ProfileSkill.builder().skill(tailwindSkill).proficiencyLevel(2).build()
                ))
                .status(DomainStatus.ACTIVE)
                .build();

        User user2 = User.builder().id(UUID.randomUUID()).name("Maria Souza").status(DomainStatus.ACTIVE).build();
        Profile profile2 = Profile.builder()
                .id(UUID.randomUUID())
                .user(user2)
                .jobTitle("Desenvolvedora Júnior")
                .resourceStatus(ResourceStatus.AVAILABLE)
                .skills(List.of(
                        ProfileSkill.builder().skill(reactSkill).proficiencyLevel(2).build()
                ))
                .status(DomainStatus.ACTIVE)
                .build();

        when(jobPostingRepo.findById(jobPostingId)).thenReturn(Optional.of(jobPosting));
        when(profileRepo.findByStatus(DomainStatus.ACTIVE)).thenReturn(List.of(profile1, profile2));

        Pageable pageable = PageRequest.of(0, 10);
        Page<JobPostingCandidateMatchResponse> result = jobPostingService.findCandidatesForJobPosting(
                jobPostingId, null, null, null, pageable
        );

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);

        JobPostingCandidateMatchResponse first = result.getContent().get(0);
        assertThat(first.name()).isEqualTo("João Silva");
        assertThat(first.rankingScore()).isEqualTo(1);
        assertThat(first.matchScore()).isEqualTo(100);
        assertThat(first.squadName()).isEqualTo("Squad Pagamentos");
        assertThat(first.avaiability()).isEqualTo("available");
        assertThat(first.availability()).isEqualTo("available");
        assertThat(first.summary().requiredSkillsMatched()).isEqualTo(2);
        assertThat(first.summary().requiredSkillsTotal()).isEqualTo(2);
        assertThat(first.summary().desirableSkillsMatched()).isEqualTo(1);
        assertThat(first.summary().desirableSkillsTotal()).isEqualTo(1);
        assertThat(first.mainSkills()).isNotEmpty();

        JobPostingCandidateMatchResponse second = result.getContent().get(1);
        assertThat(second.name()).isEqualTo("Maria Souza");
        assertThat(second.rankingScore()).isEqualTo(2);
        assertThat(second.matchScore()).isLessThan(first.matchScore());
    }

    @Test
    @DisplayName("Deve filtrar candidatos por minScore")
    void shouldFilterCandidatesByMinScore() {
        User user1 = User.builder().id(UUID.randomUUID()).name("João Silva").status(DomainStatus.ACTIVE).build();
        Profile profile1 = Profile.builder()
                .id(UUID.randomUUID())
                .user(user1)
                .jobTitle("Engenheiro Pleno")
                .skills(List.of(
                        ProfileSkill.builder().skill(reactSkill).proficiencyLevel(4).build(),
                        ProfileSkill.builder().skill(typeScriptSkill).proficiencyLevel(4).build()
                ))
                .status(DomainStatus.ACTIVE)
                .build();

        User user2 = User.builder().id(UUID.randomUUID()).name("Maria Souza").status(DomainStatus.ACTIVE).build();
        Profile profile2 = Profile.builder()
                .id(UUID.randomUUID())
                .user(user2)
                .jobTitle("Desenvolvedora Júnior")
                .skills(List.of(
                        ProfileSkill.builder().skill(tailwindSkill).proficiencyLevel(1).build()
                ))
                .status(DomainStatus.ACTIVE)
                .build();

        when(jobPostingRepo.findById(jobPostingId)).thenReturn(Optional.of(jobPosting));
        when(profileRepo.findByStatus(DomainStatus.ACTIVE)).thenReturn(List.of(profile1, profile2));

        Pageable pageable = PageRequest.of(0, 10);
        Page<JobPostingCandidateMatchResponse> result = jobPostingService.findCandidatesForJobPosting(
                jobPostingId, 50, null, null, pageable
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando vaga não existir")
    void shouldThrowExceptionWhenJobNotFound() {
        when(jobPostingRepo.findById(jobPostingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostingService.findCandidatesForJobPosting(jobPostingId, null, null, null, PageRequest.of(0, 10)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vaga não encontrada");
    }
}
