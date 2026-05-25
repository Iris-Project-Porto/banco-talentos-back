package com.vilt.talentos.service;

import com.vilt.talentos.dto.GroupRequest;
import com.vilt.talentos.dto.GroupResponse;
import com.vilt.talentos.entity.Group;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.repository.GroupRepository;
import com.vilt.talentos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepo;
    private final UserRepository userRepo;

    public List<GroupResponse> findAllActive() {
        return groupRepo.findByActive(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<GroupResponse> findAllInactive() {
        return groupRepo.findByActive(false).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public GroupResponse findById(UUID id) {
        return groupRepo.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo não encontrado"));
    }

    public GroupResponse create(GroupRequest request) {
        User currentUser = getCurrentUser();
        Group group = Group.builder()
                .name(request.name())
                .description(request.description())
                .active(true)
                .createdBy(currentUser)
                .build();
        return mapToResponse(groupRepo.save(group));
    }

    public GroupResponse update(UUID id, GroupRequest request) {
        Group group = groupRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo não encontrado"));
        
        group.setName(request.name());
        group.setDescription(request.description());
        group.setUpdatedBy(getCurrentUser());
        
        return mapToResponse(groupRepo.save(group));
    }

    public void setActiveStatus(UUID id, boolean active) {
        Group group = groupRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo não encontrado"));
        group.setActive(active);
        group.setUpdatedBy(getCurrentUser());
        groupRepo.save(group);
    }

    private GroupResponse mapToResponse(Group g) {
        return new GroupResponse(
                g.getId(),
                g.getName(),
                g.getDescription(),
                g.isActive(),
                g.getCreatedAt(),
                g.getUpdatedAt(),
                g.getCreatedBy() != null ? g.getCreatedBy().getName() : "Sistema",
                g.getUpdatedBy() != null ? g.getUpdatedBy().getName() : null
        );
    }

    private User getCurrentUser() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findById(UUID.fromString(userIdStr))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado"));
    }
}
