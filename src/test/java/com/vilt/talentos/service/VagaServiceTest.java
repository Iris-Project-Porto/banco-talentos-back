package com.vilt.talentos.service;

import com.vilt.talentos.dto.VagaRequest;
import com.vilt.talentos.dto.VagaResponse;
import com.vilt.talentos.entity.Vaga;
import com.vilt.talentos.repository.VagaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VagaServiceTest {

    @Mock private VagaRepository vagaRepository;

    @InjectMocks private VagaService vagaService;

    private Vaga vagaExemplo;
    private UUID vagaId;

    @BeforeEach
    void setUp() {
        vagaId = UUID.randomUUID();
        vagaExemplo = Vaga.builder()
                .id(vagaId)
                .titulo("Desenvolvedor Java")
                .senioridade(Vaga.Senioridade.Pleno)
                .time("Squad Pagamentos")
                .solicitante("Ana Lima")
                .tempoContratacao("CLT")
                .numeroVagas(2)
                .area("Engenharia")
                .skills(new String[]{"Java", "Spring"})
                .descricao("Vaga para desenvolvedor backend.")
                .status(Vaga.StatusVaga.ABERTA)
                .prioridade(Vaga.Prioridade.ALTA)
                .dataAbertura(LocalDate.of(2026, 5, 1))
                .build();
    }

    // ─── listar ──────────────────────────────────────────────────────────────

    @Test
    void listar_deveRetornarListaMapeada() {
        when(vagaRepository.findAllByOrderByDataAberturaDesc()).thenReturn(List.of(vagaExemplo));

        List<VagaResponse> result = vagaService.listar();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).titulo()).isEqualTo("Desenvolvedor Java");
        assertThat(result.get(0).status()).isEqualTo("Aberta");
        assertThat(result.get(0).senioridade()).isEqualTo("Pleno");
    }

    @Test
    void listar_semVagas_deveRetornarListaVazia() {
        when(vagaRepository.findAllByOrderByDataAberturaDesc()).thenReturn(List.of());

        assertThat(vagaService.listar()).isEmpty();
    }

    // ─── buscarPorId ─────────────────────────────────────────────────────────

    @Test
    void buscarPorId_existente_deveRetornarResponse() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vagaExemplo));

        VagaResponse response = vagaService.buscarPorId(vagaId);

        assertThat(response.id()).isEqualTo(vagaId);
        assertThat(response.titulo()).isEqualTo("Desenvolvedor Java");
    }

    @Test
    void buscarPorId_inexistente_deveLancar404() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vagaService.buscarPorId(vagaId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ─── criar ───────────────────────────────────────────────────────────────

    @Test
    void criar_deveRetornarResponseComIdPreenchido() {
        var req = new VagaRequest(
                "Dev Frontend", Vaga.Senioridade.Jr, "Squad UI", "Carlos",
                "PJ", 1, "Front", List.of("React", "TypeScript"),
                "Vaga frontend", Vaga.StatusVaga.ABERTA, Vaga.Prioridade.MEDIA,
                LocalDate.of(2026, 5, 20)
        );

        Vaga salva = Vaga.builder()
                .id(UUID.randomUUID())
                .titulo("Dev Frontend")
                .senioridade(Vaga.Senioridade.Jr)
                .time("Squad UI")
                .solicitante("Carlos")
                .tempoContratacao("PJ")
                .numeroVagas(1)
                .area("Front")
                .skills(new String[]{"React", "TypeScript"})
                .status(Vaga.StatusVaga.ABERTA)
                .prioridade(Vaga.Prioridade.MEDIA)
                .dataAbertura(LocalDate.of(2026, 5, 20))
                .build();

        when(vagaRepository.save(any(Vaga.class))).thenReturn(salva);

        VagaResponse response = vagaService.criar(req);

        assertThat(response.id()).isNotNull();
        assertThat(response.titulo()).isEqualTo("Dev Frontend");
        verify(vagaRepository).save(any(Vaga.class));
    }

    @Test
    void criar_semDataAbertura_deveUsarDataDeHoje() {
        var req = new VagaRequest(
                "Dev Backend", Vaga.Senioridade.Sr, "Core", "Maria",
                null, 1, null, null, null,
                Vaga.StatusVaga.ABERTA, Vaga.Prioridade.BAIXA, null
        );

        when(vagaRepository.save(any(Vaga.class))).thenAnswer(inv -> inv.getArgument(0));

        vagaService.criar(req);

        var captor = ArgumentCaptor.forClass(Vaga.class);
        verify(vagaRepository).save(captor.capture());
        assertThat(captor.getValue().getDataAbertura()).isEqualTo(LocalDate.now());
    }

    // ─── atualizar ───────────────────────────────────────────────────────────

    @Test
    void atualizar_existente_deveAtualizarCampos() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vagaExemplo));
        when(vagaRepository.save(any(Vaga.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new VagaRequest(
                "Java Sênior", Vaga.Senioridade.Sr, "Core", "Pedro",
                "CLT", 3, "Backend", List.of("Java"),
                "Atualizada", Vaga.StatusVaga.EM_ANDAMENTO, Vaga.Prioridade.URGENTE,
                LocalDate.of(2026, 5, 10)
        );

        VagaResponse response = vagaService.atualizar(vagaId, req);

        assertThat(response.titulo()).isEqualTo("Java Sênior");
        assertThat(response.status()).isEqualTo("Em andamento");
        assertThat(response.senioridade()).isEqualTo("Sr");
        assertThat(response.numeroVagas()).isEqualTo(3);
    }

    @Test
    void atualizar_inexistente_deveLancar404() {
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.empty());

        var req = new VagaRequest(
                "Qualquer", Vaga.Senioridade.Jr, "Time", "Pessoa",
                null, 1, null, null, null,
                Vaga.StatusVaga.ABERTA, Vaga.Prioridade.BAIXA, null
        );

        assertThatThrownBy(() -> vagaService.atualizar(vagaId, req))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ─── deletar ─────────────────────────────────────────────────────────────

    @Test
    void deletar_existente_deveChamarDeleteById() {
        when(vagaRepository.existsById(vagaId)).thenReturn(true);

        vagaService.deletar(vagaId);

        verify(vagaRepository).deleteById(vagaId);
    }

    @Test
    void deletar_inexistente_deveLancar404() {
        when(vagaRepository.existsById(vagaId)).thenReturn(false);

        assertThatThrownBy(() -> vagaService.deletar(vagaId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(vagaRepository, never()).deleteById(any());
    }
}
