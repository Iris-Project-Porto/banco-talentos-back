package com.vilt.talentos.service;

import com.vilt.talentos.dto.ProjectResponse;
import com.vilt.talentos.dto.SquadMemberCount;
import com.vilt.talentos.dto.SquadResponse;
import com.vilt.talentos.entity.Project;
import com.vilt.talentos.entity.Squad;
import com.vilt.talentos.mapper.ProjectMapperImpl;
import com.vilt.talentos.mapper.SquadMapperImpl;
import com.vilt.talentos.repository.ProfileRepository;
import com.vilt.talentos.repository.ProjectRepository;
import com.vilt.talentos.repository.SquadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SquadMemberCountServiceTest {
    @Mock SquadRepository squadRepo;
    @Mock ProjectRepository projectRepo;
    @Mock(answer = Answers.CALLS_REAL_METHODS) ProfileRepository profileRepo;

    private SquadService squadService;
    private ProjectService projectService;
    private final Squad populated = Squad.builder().id(UUID.randomUUID()).name("Populated").build();
    private final Squad empty = Squad.builder().id(UUID.randomUUID()).name("Empty").build();

    @BeforeEach
    void setUp() {
        SquadMapperImpl mapper = new SquadMapperImpl();
        squadService = new SquadService(squadRepo, profileRepo, null, null, null, mapper, null);
        projectService = new ProjectService(projectRepo, null, null, profileRepo, null, squadRepo,
                new ProjectMapperImpl(), mapper, null);
    }

    @ParameterizedTest
    @CsvSource({"true,", "true, search", "false,", "false, search"})
    void squadPagesIncludeCountsWithoutLoadingResources(boolean active, String search) {
        PageRequest pageable = PageRequest.of(1, 2);
        Page<Squad> page = new PageImpl<>(List.of(populated, empty), pageable, 8);
        if (search == null) {
            when(squadRepo.findByActive(active, pageable)).thenReturn(page);
        } else {
            when(squadRepo.findByActiveAndNameContainingIgnoreCase(active, search, pageable)).thenReturn(page);
        }
        List<UUID> ids = List.of(populated.getId(), empty.getId());
        when(profileRepo.findSquadMemberCounts(ids)).thenReturn(List.of(new SquadMemberCount(populated.getId(), 3)));

        Page<SquadResponse> response = active
                ? squadService.findAllActive(search, pageable)
                : squadService.findAllInactive(search, pageable);

        assertThat(response.getContent()).extracting(SquadResponse::members).containsExactly(3L, 0L);
        assertThat(response.getContent()).allSatisfy(squad -> assertThat(squad.recursos()).isEmpty());
        assertThat(response.getTotalElements()).isEqualTo(8);
        assertThat(response.getNumber()).isEqualTo(1);
        verifySingleBatch(ids);
    }

    @Test
    void unlinkedSquadsIncludeCounts() {
        when(squadRepo.findByActiveAndProjectIsNull(true)).thenReturn(List.of(populated, empty));
        List<UUID> ids = List.of(populated.getId(), empty.getId());
        when(profileRepo.findSquadMemberCounts(ids)).thenReturn(List.of(new SquadMemberCount(populated.getId(), 3)));

        assertThat(squadService.findAllUnlinked()).extracting(SquadResponse::members).containsExactly(3L, 0L);

        verifySingleBatch(ids);
    }

    @Test
    void emptySquadListDoesNotExecuteCountQuery() {
        when(squadRepo.findByActiveAndProjectIsNull(true)).thenReturn(List.of());

        assertThat(squadService.findAllUnlinked()).isEmpty();

        verify(profileRepo, never()).findSquadMemberCounts(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void projectPagesCountSquadsAcrossAllProjectsInOneBatch(boolean active) {
        Project first = project("First", List.of(populated));
        Project second = project("Second", List.of(empty));
        PageRequest pageable = PageRequest.of(0, 2);
        when(projectRepo.findByActive(active, pageable))
                .thenReturn(new PageImpl<>(List.of(first, second), pageable, 6));
        List<UUID> ids = List.of(populated.getId(), empty.getId());
        when(profileRepo.findSquadMemberCounts(ids)).thenReturn(List.of(new SquadMemberCount(populated.getId(), 3)));

        Page<ProjectResponse> response = active
                ? projectService.findAllActive(pageable)
                : projectService.findAllInactive(pageable);

        assertThat(response.getContent().get(0).squads().get(0).members()).isEqualTo(3);
        assertThat(response.getContent().get(1).squads().get(0).members()).isZero();
        assertThat(response.getTotalElements()).isEqualTo(6);
        verifySingleBatch(ids);
    }

    @Test
    void projectDetailIncludesSquadCounts() {
        Project project = project("Project", List.of(populated, empty));
        when(projectRepo.findWithSquadsById(project.getId())).thenReturn(Optional.of(project));
        List<UUID> ids = List.of(populated.getId(), empty.getId());
        when(profileRepo.findSquadMemberCounts(ids)).thenReturn(List.of(new SquadMemberCount(populated.getId(), 3)));

        ProjectResponse response = projectService.findById(project.getId());

        assertThat(response.squads()).extracting(SquadResponse::members).containsExactly(3L, 0L);
        verifySingleBatch(ids);
    }

    @Test
    void projectsWithoutSquadsDoNotExecuteCountQuery() {
        Project project = project("Empty project", List.of());
        PageRequest pageable = PageRequest.of(0, 2);
        when(projectRepo.findByActive(true, pageable)).thenReturn(new PageImpl<>(List.of(project), pageable, 1));
        when(projectRepo.findWithSquadsById(project.getId())).thenReturn(Optional.of(project));

        assertThat(projectService.findAllActive(pageable).getContent().get(0).squads()).isEmpty();
        assertThat(projectService.findById(project.getId()).squads()).isEmpty();

        verify(profileRepo, never()).findSquadMemberCounts(any());
    }

    private Project project(String name, List<Squad> squads) {
        return Project.builder().id(UUID.randomUUID()).name(name).squads(squads).build();
    }

    private void verifySingleBatch(List<UUID> ids) {
        verify(profileRepo).countMembersBySquadIds(ids);
        verify(profileRepo).findSquadMemberCounts(ids);
        verifyNoMoreInteractions(profileRepo);
    }
}
