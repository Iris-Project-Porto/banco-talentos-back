package com.vilt.talentos.controller;

import com.vilt.talentos.dto.GroupResponse;
import com.vilt.talentos.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Groups", description = "Endpoints públicos para consulta de grupos")
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    @Operation(summary = "Listar grupos ativos", description = "Retorna a lista de grupos ativos para seleção no cadastro.")
    public List<GroupResponse> getAllGroups() {
        return groupService.findAllActive();
    }
}
