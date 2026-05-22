package com.vilt.talentos.service;

import com.vilt.talentos.dto.VagaRequest;
import com.vilt.talentos.dto.VagaResponse;
import com.vilt.talentos.entity.Vaga;
import com.vilt.talentos.repository.VagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VagaService {

    private final VagaRepository vagaRepository;

    public List<VagaResponse> listar() {
        return vagaRepository.findAllByOrderByDataAberturaDesc()
                .stream()
                .map(VagaResponse::from)
                .toList();
    }

    public VagaResponse buscarPorId(UUID id) {
        return vagaRepository.findById(id)
                .map(VagaResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaga não encontrada."));
    }

    public VagaResponse criar(VagaRequest req) {
        Vaga vaga = Vaga.builder()
                .titulo(req.titulo())
                .senioridade(req.senioridade())
                .time(req.time())
                .solicitante(req.solicitante())
                .tempoContratacao(req.tempoContratacao())
                .numeroVagas(req.numeroVagas())
                .area(req.area())
                .skills(req.skills() != null ? req.skills().toArray(new String[0]) : null)
                .descricao(req.descricao())
                .status(req.status())
                .prioridade(req.prioridade())
                .dataAbertura(req.dataAbertura() != null ? req.dataAbertura() : LocalDate.now())
                .build();

        return VagaResponse.from(vagaRepository.save(vaga));
    }

    public VagaResponse atualizar(UUID id, VagaRequest req) {
        Vaga vaga = vagaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaga não encontrada."));

        vaga.setTitulo(req.titulo());
        vaga.setSenioridade(req.senioridade());
        vaga.setTime(req.time());
        vaga.setSolicitante(req.solicitante());
        vaga.setTempoContratacao(req.tempoContratacao());
        vaga.setNumeroVagas(req.numeroVagas());
        vaga.setArea(req.area());
        vaga.setSkills(req.skills() != null ? req.skills().toArray(new String[0]) : null);
        vaga.setDescricao(req.descricao());
        vaga.setStatus(req.status());
        vaga.setPrioridade(req.prioridade());
        if (req.dataAbertura() != null) {
            vaga.setDataAbertura(req.dataAbertura());
        }

        return VagaResponse.from(vagaRepository.save(vaga));
    }

    public void deletar(UUID id) {
        if (!vagaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaga não encontrada.");
        }
        vagaRepository.deleteById(id);
    }
}
